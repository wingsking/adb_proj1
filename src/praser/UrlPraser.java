package praser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import bing.*;

import org.htmlparser.*;
import org.htmlparser.beans.*;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;

public class UrlPraser {
	private String getNewsContent(NodeFilter newsContentFilter, Parser parser) {
		String content = null;
		StringBuilder builder = new StringBuilder();

		try {
			parser.setEncoding("utf-8");
			NodeList newsContentList = (NodeList) parser
					.parse(newsContentFilter);
			BodyTag newsContenTag = (BodyTag) newsContentList.elementAt(0);
			builder = builder.append(newsContenTag.getStringText());

			content = builder.toString();
			if (content != null) {
				parser.reset();

				parser = Parser.createParser(content, "utf-8");
				StringBean sb = new StringBean();
				sb.setCollapse(true);
				parser.visitAllNodesWith(sb);
				content = sb.getStrings();

			} else {
				// System.out.println("no content!");
			}

		} catch (ParserException ex) {
			// Logger.getLogger(ParsePage.class.getName()).log(Level.SEVERE,
			// null, ex);
		}

		return content;
	}

	public String parser(String url) {
		String newsContent = "";
		try {
			Parser parser = new Parser(url);
			NodeFilter contentFilter = new TagNameFilter("body");

			parser.reset(); // reset praser after every prasering!
			newsContent = getNewsContent(contentFilter, parser);

		} catch (ParserException ex) {
			// Logger.getLogger(ParsePage.class.getName()).log(Level.SEVERE,
			// null, ex);
		} catch (NullPointerException e) {

		}
		return newsContent;
	}

	// this method will generate the wordList for a doc
	public HashMap<String, ArrayList<Integer>> getWordList(Result result) {
		String content = parser(result.getUrl());
		String[] wordArr = content.split("\\W+");
		// storing this doc's length
		result.setLength(wordArr.length);

		// now generate the word list
		HashMap<String, ArrayList<Integer>> ret = new HashMap<String, ArrayList<Integer>>();

		String[] wikiIgnores = new String[] { "retrieved", "archived",
				"january", "february", "march", "april", "may", "june", "july",
				"august", "september", "october", "november", "december" };
		HashSet<String> wikipediaIgnore = new HashSet<String>(
				Arrays.asList(wikiIgnores));

		for (int i = 0; i < wordArr.length; i++) {
			String str = wordArr[i].toLowerCase();

			if (result.getUrl().matches("^http://en\\.wikipedia\\.org.*")) {
				if (wikipediaIgnore.contains(str)
						|| str.matches("^19[0-9][0-9]|^20[0-9][0-9]")) {
					/**
					 * ignore the word "retrieved" and "archived" from wikipedia
					 * also ignore all months, this appear in wikipedia's
					 * references every frequently,
					 *  every references in wikipedia
					 * has a word "retrieve" or "archived" also, ignore the
					 * years 19XX and 20XX, they appear in many many references
					 */

					continue;

				}
			}

			// don't care single digit, they might be chapter number etc.
			// don't care single letter either, because they are meaningless

			if (!str.matches("\\d{1,2}|\\w{1}|_*")) {
				if (ret.containsKey(str)) {
					ret.get(str).add(i);
				} else {
					ArrayList<Integer> posList = new ArrayList<Integer>();
					posList.add(i);
					ret.put(str, posList);
				}
			}
		}

		// mark the maximum tf, in order for the calculation of maxTf algorithm
		Iterator<Map.Entry<String, ArrayList<Integer>>> it = ret.entrySet()
				.iterator();
		int maxTf = 0;
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<Integer>> pairs = (Map.Entry<String, ArrayList<Integer>>) it
					.next();
			ArrayList<Integer> posList = pairs.getValue();
			maxTf = Math.max(maxTf, posList.size());
		}
		result.setMaxTf(maxTf);

		return ret;
	}

	public static void main(String[] args) {
		// testing the url praser and word separator
		/*
		 * UrlPraser praser = new UrlPraser(); String content =
		 * praser.parser("http://en.wikipedia.org/wiki/Bill_Gates");
		 * System.out.println(Arrays.toString(content.split("\\W+")));
		 */

		UrlPraser praser = new UrlPraser();
		Result result = new Result("http://en.wikipedia.org/wiki/Bill_Gates",
				"", "");

		HashMap<String, ArrayList<Integer>> content = praser
				.getWordList(result);
		Iterator<Map.Entry<String, ArrayList<Integer>>> it = content.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<Integer>> pairs = (Map.Entry<String, ArrayList<Integer>>) it
					.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
	}
}