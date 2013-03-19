package com.github.thieumdesp.imdbrequester.model;


public class Movie implements Comparable<Movie> {
	
	private String givenTitle = "";
	private String title = "";
	private String rating = "";
	private String directory = "";
	private String extension = "";

	public Movie(String givenTitle, String title, String rating) {
		super();
		this.givenTitle = givenTitle;
		this.title = title;
		this.rating = rating;
	}

	public Movie(String givenTitle, String directory) {
		super();
		this.givenTitle = givenTitle;
		this.directory = directory;
	}

	public Movie(String givenTitle) {
		super();
		this.givenTitle = givenTitle;
	}

	@Override
	public int compareTo(Movie arg0) {
		Double dbRating = new Double(rating);
		if (dbRating.equals(new Double(arg0.getRating()))) {
			return 0;
		} else  
			return ((dbRating >= new Double(arg0.getRating()))?1:-1);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getGivenTitle() {
		return givenTitle;
	}

	public void setGivenTitle(String givenTitle) {
		this.givenTitle = givenTitle;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	
}
