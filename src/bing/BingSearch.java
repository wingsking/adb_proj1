package bing;

import expansionAlgorithm.*;
import indexer.IndexCreator;
import indexer.Posting;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//Download and add these two libraries to the build path.
import org.apache.commons.codec.binary.Base64;

import org.json.*;

/*
 *	need to implement the augmentQuery function
 *
 */

public class BingSearch {

	public static void main(String[] args) throws IOException, JSONException {

		// get parameters from command line
		String accountKey = args[0];
		double preset = Double.parseDouble(args[1]);
		double precision = preset;
		String query = args[2].replaceAll("[^\\p{L}\\p{Z}\\']", " ")
				.replaceAll("\\s+", " ");

		/*
		 * String accountKey = "cnLsEsvYTSd+XBWkE4lO7z02Wgh3W14UTAwgJ/JURdc=";
		 * double preset = Double.parseDouble("0.9"); double precision = preset;
		 * 
		 * String query = "wow".replaceAll("[^\\p{L}\\p{Z}]",
		 * " ").replaceAll("\\s+", " "); // change to any query you like
		 */

		// chhanges the query term to lower case form
		query = query.toLowerCase();
		Scanner scan = new Scanner(System.in);

		while (true) {
			// get content from bing
			String content = getContent(accountKey, preset, query);

			// store results returned by bing
			ArrayList<Result> results = new ArrayList<Result>();

			// make the query becomes lower case
			query.toLowerCase();

			// the number of results that are relevant
			int count = 0;

			// the content returned by Bing is in JSON format
			JSONObject obj = new JSONObject(content);

			// an array of results returned by bing search
			JSONArray objs = obj.getJSONObject("d").getJSONArray("results");
			// total number of results returned by bing
			int n = objs.length();
			System.out.println("Total no of results : " + n);

			// if less than 10 results returned, terminate
			if (n < 10) {
				System.out.println("Less than 10 Results returned, terminate.");
				return;
			}
			System.out
					.println("Bing Search Results: \n=========================");

			// store all results into ArrayList<Result>
			for (int i = 0; i < n; ++i) {
				JSONObject rs = objs.getJSONObject(i);
				results.add(new Result(rs.getString("Url"), rs
						.getString("Title"), rs.getString("Description")));

				// display result to user
				System.out.println("Result " + (i + 1));
				results.get(i).print();

				// user choose whether this result is relevant
				System.out.print("Relevant (Y/N)?");
				String bool = scan.next();

				// the result is relevant
				if (bool.toLowerCase().equals("y")) {
					results.get(i).setRelevant(true);
					count++;
				} else {
					results.get(i).setRelevant(false);
				}

			}

			// if there is no relevant result, terminate
			if (count == 0) {
				System.out.println("No relevant results, terminiate.");
				return;
			}

			precision = (double) count / n;

			// if desired precision not met
			if ((precision - preset) < -0.000001) {
				System.out
						.println("=========================\nFEEDBACK SUMMARY"
								+ "\nQuery: " + query + "\nPrecision: "
								+ precision
								+ "\nStill below the desired precision of "
								+ preset + "\nIndexing results ...."
								+ "\nIndexing results ....");

				// implement query augmentation algorithm, return augmented
				// query as an array of string

				// build Inverted File
				HashMap<String, Posting> index = IndexCreator
						.createIndex(results);
				ArrayList<String> augmentedWords = new ArrayList<String>();
				try {
					query = Algorithm.expandQuery(index, query, results,
							augmentedWords);
				} catch (RuntimeException ex) {
					System.out.println("Query contains stop words!");
					System.out.println("This program cannot process stop words.");
					System.out.println("Program terminates!");
					return;
				}

				System.out.print("Augmenting by ");
				for (String str : augmentedWords) {
					System.out.print(str + " ");
				}
				System.out.println();
				// String[] augments = augmentQuery(results);

				// output augmented keywords
				// System.out.println("Augmenting by " + augments);

				// construct new query
				// query += augments

			} else {
				System.out
						.println("=========================\nFEEDBACK SUMMARY"
								+ "\nQuery: " + query + "\nPrecision: "
								+ precision
								+ "\nDesired precision reached, done");
				return;
			}

		}

	}

	// encode the query to make it url-acceptable, or decode it
	public static String parseQuery(String s) {
		if (s.contains(" "))
			return s.replaceAll(" ", "%20");
		return s;
	}

	// connect to Bing and get return content as a string
	public static String getContent(String accountKey, double precision,
			String query) throws IOException {
		// need to encode query with URL-style
		String encodeQuery = parseQuery(query); // "gates microsoft"
												// ->
												// "gates%20microsoft"

		String bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&$format=json&Query=%27"
				+ encodeQuery + "%27";

		byte[] accountKeyBytes = Base64
				.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(bingUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic "
				+ accountKeyEnc);

		InputStream inputStream = (InputStream) urlConnection.getContent();
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);

		// print metadata
		printMeta(accountKey, query, precision, bingUrl);

		return new String(contentRaw);

	}

	// print metadata before each search
	public static void printMeta(String accountKey, String query,
			double precision, String url) {
		System.out.println("Parameters:\nClient key\t= " + accountKey
				+ "\nQuery\t\t= " + query + "\nPrecision\t= " + precision
				+ "\nURL: " + url);
		// + "\nTotal no of results : " + total);
	}

	// query expansion function
	public static String[] augmentQuery(ArrayList<Result> r) {
		return new String[] {};
	}

}
