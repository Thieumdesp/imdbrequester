package com.github.thieumdesp.imdbrequester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.thieumdesp.imdbrequester.model.Movie;

public class MovieRating {
	
	public static String JSON_ATTRIBUTE_TITLE = "title";
	public static String JSON_ATTRIBUTE_RATING = "rating";
	public static String SEPARATOR = ".";
	
	public static List<String[]> getRating(Movie movie) {
		List<String[]> retour = new ArrayList<String[]>();
		
		InputStream is = getResponse(movie.getGivenTitle());
		
		try {
			JSONArray jsonArray = readJsonArrayFromInputStream(is);
			
			String[] rating = new String[2];
			if (jsonArray != null ){
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					
					rating[0] = jsonObject.get(JSON_ATTRIBUTE_TITLE).toString();
					rating[1] = jsonObject.get(JSON_ATTRIBUTE_RATING).toString();
					retour.add(rating);
				}
			} else {
				rating[0] = movie.getGivenTitle();
				rating[1] = "-1";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			String[] rating = new String[2];
			rating[0] = movie.getGivenTitle();
			rating[1] = "-2";
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return retour;
	}
	
	public static InputStream getResponse(String filmTitle)  {
		
		int TIMEOUT_MILLISEC = 100000;  // = 10 seconds
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpClient client = new DefaultHttpClient(httpParams);
		
		HttpGet request = new HttpGet("http://imdbapi.org/?q=" + URLEncoder.encode(filmTitle));
		request.setHeader("Content-Type", "application/json");
		
		try {
			HttpResponse response = client.execute(request);

			
			return response.getEntity().getContent();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		return null;		
	}
	
	private static JSONArray readJsonArrayFromInputStream(InputStream is)
			throws IOException, JSONException {
		JSONArray json = null;
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			if (!jsonText.startsWith("[")) {
				jsonText = "[" + jsonText + "]";
			}
			
			json = new JSONArray(jsonText);
			return json;
		} catch (JSONException e) {

		} finally {
			is.close();
		}
		return null;
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	/**
	 * Read a file to get the list of movies
	 * @param absoluteFileName
	 * @return List of movies
	 */
	private static List<Movie> readFile(String absoluteFileName) {

		BufferedReader br = null;
		List<Movie> movies = new ArrayList<Movie>();

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(absoluteFileName));

			while ((sCurrentLine = br.readLine()) != null) {
				Movie movie = new Movie(removeExtension(sCurrentLine));
				movie.setExtension(getExtension(sCurrentLine));
				movie.setDirectory("");
				movies.add(movie);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return movies;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args == null || !(args.length == 1)) {
			System.out.println(usage());
			System.exit(-1);
		}
		String path = args[0];
		File file = new File(path);
		
		List<Movie> moviesInFile = new ArrayList<Movie>();
		if (file.isDirectory()) {
			analyseEntite(file, moviesInFile);
		} else {
			moviesInFile = readFile(path);
		}
		
		List<Movie> movies = new ArrayList<Movie>();
		for (Movie movie : moviesInFile) {
			List<String[]> rating = getRating(movie);
			
			System.out.println("get rating for " + movie.getGivenTitle());
			for (String[] strings : rating) {
				Movie newMovie = new Movie(movie.getGivenTitle(), strings[0], strings[1]);
				newMovie.setDirectory(movie.getDirectory());
				newMovie.setExtension(movie.getExtension());
				movies.add(newMovie);
			}
		}
		System.out.println("sort movies, size " + movies.size());
		Collections.sort(movies);
		
		for (Movie movie : movies) {
			System.out.println(movie.getGivenTitle() + ";" + movie.getRating() +  ";cp \"" + movie.getDirectory() +  "\" /cygdrive/d/multimedia/films/");
		}
	}
	
	/**
	 * Analyse a file or a directory
	 * @param entite
	 * @param moviesInFile
	 */
	private static void analyseEntite(File entite, List<Movie> moviesInFile) {
		if (entite.isFile()) {
			Movie movie = new Movie(removeExtension(entite.getName()), entite.getAbsolutePath());
			movie.setExtension(getExtension(entite.getName()));
			moviesInFile.add(movie);
		} else {
			File[] fichiers = entite.listFiles();
			for (int i = 0; i < fichiers.length; i++) {
				analyseEntite(fichiers[i], moviesInFile);
			}
		}
	}

	private static String getExtension(String label) {
		if (label.lastIndexOf(SEPARATOR) > 0)
			return label.substring(label.lastIndexOf(SEPARATOR), label.length());
		return "";
	}
	
	private static String removeExtension(String label) {
		if (label.lastIndexOf(SEPARATOR) > 0)
			return label.substring(0, label.lastIndexOf(SEPARATOR));
		return label;
	}
	
	public static String usage() {
		return "MovieRating [file|directory]";
	}

}
