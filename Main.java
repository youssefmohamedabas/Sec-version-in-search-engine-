import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {
    public static class Dictenter {
        int doc_freq = 0;
        int term_freq = 1;
        Posting pList = null;
    }

    public static class Posting {
        Posting next = null;
        int docId;
        int dtf = 1;
    }

    public static boolean stop_words(String s) {
        String[] commonWords = { "i", "we", "our", "you", "your", "it", "its", "itself", "they", "them", "their",
                "theirs", "themselves", "what", "which", "this", "that", "these", "those", "am", "is", "are", "was",
                "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an",
                "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with",
                "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to",
                "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once",
                "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most",
                "other", "some", "such", "no", "not", "only", "own", "same", "so", "than", "too", "very", "can", "will",
                "should", "now" };

        for (String word : commonWords) {
            if (s.equals(word)) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        String[] filenames = { "file1.txt", "file2.txt", "file3.txt", "file4.txt", "file5.txt",
                "file6.txt", "file7.txt", "file8.txt", "file9.txt", "file10.txt" };

        // Replace with your file path

        Scanner scanner = new Scanner(System.in);

        InvertedIndex invertedIndex = new InvertedIndex();
        try {
            for (int i = 0; i < filenames.length; i++) {
                String filename = filenames[i];

                // random access file
                RandomAccessFile file = new RandomAccessFile(filename, "r");
                int docId = i + 1; // docId starts from 1
                String line;
                int position = 1; // position of the term in the document

                // read each line and split it into words
                while ((line = file.readLine()) != null) {
                    // split line into words and remove punctuation marks
                    line = line.replaceAll("[^a-zA-Z0-9]", " ");
                    // remove extra spaces
                    line = line.replaceAll("\\s+", " ");
                    line = line.trim();
                    line = line.toLowerCase();
                    // split line into words
                    String[] words = line.split(" ");

                    // add each word to the inverted index
                    for (String word : words) {
                        invertedIndex.addTerm(word, docId, position);
                        position++;
                    }
                }
                file.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, Dictenter> index = new HashMap<String, Dictenter>();

        for (int i = 1; i <= 10; i++) {
            String fileName = "file" + i + ".txt";
            int docId = i;
            try {
                File file = new File(fileName);
                Scanner scanner1 = new Scanner(file);
                while (scanner1.hasNext()) {
                    String term = scanner1.next().toLowerCase();
                    if (!index.containsKey(term)) {
                        index.put(term, new Dictenter());
                    }
                    Dictenter obj = index.get(term);
                    obj.term_freq++;
                    if (obj.pList == null) {
                        obj.pList = new Posting();
                        obj.pList.docId = docId;
                    } else {
                        Posting p = obj.pList;
                        while (p.next != null && p.docId != docId) {
                            p = p.next;
                        }
                        if (p.docId == docId) {
                            p.dtf++;
                        } else {
                            p.next = new Posting();
                            p.next.docId = docId;
                        }
                    }

                }

                scanner1.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + fileName);
            }
        }

        int choice = 0;
        while (choice != 5) {
            System.out.println("1-Enter a query");
            System.out.println("2-Print TF-IDF");
            System.out.println("3-Search for a word");
            System.out.println("4-Webcrawler");
            System.out.println("5-Exit the program");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            if (choice == 1) {
                System.out.println();
                System.out.print("Enter a query: ");
                String query = scanner.nextLine();
                query = query.toLowerCase();
                System.out.println();
                Map<Integer, Double> documentScores = new HashMap<>();

                String[] queryTerms = query.split("\\W+"); // split query into words

                // calculate cosine similarity for each document and the query
                for (int docId = 1; docId <= filenames.length; docId++) {
                    double score = 0;

                    // calculate the numerator of the cosine similarity
                    for (String term : queryTerms) {
                        int termFrequencyInQuery = invertedIndex.getTFInQuery(term, queryTerms);
                        int termFrequencyInDocument = invertedIndex.getTFInDocument(term, docId);
                        score += termFrequencyInQuery * termFrequencyInDocument;
                    }

                    // calculate the denominator of the cosine similarity magnitude for Doc that i
                    // gave
                    double documentVectorLength = invertedIndex.getDocumentVectorLength(docId);
                    double queryVectorLength = invertedIndex.getQueryVectorLength(queryTerms); // magnitude for query
                    double denominator = documentVectorLength * queryVectorLength;

                    // calculate the cosine similarity
                    score /= denominator;
                    // add the document and its score to the map
                    documentScores.put(docId, score);

                }

                // sort the documents based on cosine similarity
                List<Map.Entry<Integer, Double>> sortedDocuments = new ArrayList<>(documentScores.entrySet());
                sortedDocuments.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                // print the ranked documents
                System.out.println("*****");
                System.out.println("Documents:");
                System.out.println("*****");
                for (Map.Entry<Integer, Double> entry : sortedDocuments) {
                    int docId = entry.getKey();
                    String filename = filenames[docId - 1];
                    double score = entry.getValue();
                    System.out.println(filename + ": cosine similarity(" + score + ")");
                    System.out.println("-------------------------------------------------");
                }
                System.out.println();

            } else if (choice == 2) {
                Map<String, Map<Integer, String>> TF_IDF = new HashMap<>();
                TF_IDF = getTF_IDF(invertedIndex, filenames.length);

                // print the TF-IDF
                int maxTermLength = 0;
                for (String term : TF_IDF.keySet()) {
                    maxTermLength = Math.max(maxTermLength, term.length());
                }
                System.out.println();
                System.out.println("*****");
                System.out.println("The TF-IDF:");
                System.out.println("*****");
                System.out
                        .println("###################################################################################");
                System.out.print("Term:");
                for (int i = 0; i < maxTermLength - 3; i++) {
                    System.out.print(" ");
                }
                for (int docId = 1; docId <= filenames.length; docId++) {
                    System.out.print("f" + docId + "     ");
                }
                System.out.println();
                System.out
                        .println("###################################################################################");

                for (String term : TF_IDF.keySet()) {
                    System.out.print(term);
                    for (int i = 0; i < maxTermLength - term.length(); i++) {
                        System.out.print(" ");
                    }
                    for (int docId = 1; docId <= filenames.length; docId++) {
                        String score = TF_IDF.get(term).get(docId);
                        System.out.print(score + "  ");
                    }
                    System.out.println();
                }
                System.out
                        .println("###################################################################################");
                System.out.println();
            } else if (choice == 3) {
                System.out.println();
                System.out.print("Enter a word: ");
                String word = scanner.next().toLowerCase();
                System.out.println("*******************");
                if (stop_words(word) != true) {
                    if (index.containsKey(word)) {
                        Dictenter obj = index.get(word);
                        Posting p = obj.pList;
                        System.out.println("Files containing the word '" + word + "': ");
                        while (p != null) {
                            System.out.println("        file" + p.docId + ".txt");
                            p = p.next;
                            obj.doc_freq++;
                        }
                        System.out.println("-----------------------------------------");
                        System.out.println(" ");
                        System.out.println("Document frequency of the word '" + word + "': " + obj.doc_freq);
                        System.out.println("-----------------------------------------");
                        System.out.println("Term frequency of the word '" + word + "': " + (obj.term_freq - 1));
                        System.out.println("**************");
                        System.out.println(" ");
                        obj.doc_freq = 0;
                    } else {
                        System.out.println("The word '" + word + "' does not exist in any file.");
                    }
                } else {
                    System.out.println("This word is from very common words .");
                    System.out.println("-----------------------------------------");
                }

            } else if (choice == 4) {
                System.out.println("Links");
                WebCrawler crawler = new WebCrawler();
                for (int i = 1; i <= 10; i++) {
                    String filePath = "G:/learnjava2/file" + i + ".txt";
                    crawler.crawl(filePath);
                }
                // crawler.getPageLinks("https://en.wikipedia.org/wiki/Computer_science");

            } else if (choice == 5) {
                System.out.println("Exiting the program...");
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    public static Map<String, Map<Integer, String>> getTF_IDF(InvertedIndex invertedIndex, int LengthFilenames) {
        // get the length of each document
        Map<Integer, Integer> documentLengths = new HashMap<>();
        for (int docId = 1; docId <= LengthFilenames; docId++) {
            int length = invertedIndex.getDocumentWordsLength(docId);
            documentLengths.put(docId, length);
        }
        // get the term frequency in each document
        Map<String, Map<Integer, Integer>> termFrequencies = new HashMap<>();
        for (String term : invertedIndex.getTerms()) {
            Map<Integer, Integer> frequencies = new HashMap<>();
            for (int docId = 1; docId <= LengthFilenames; docId++) {
                int frequency = invertedIndex.getTFInDocument(term, docId);
                frequencies.put(docId, frequency);
            }
            termFrequencies.put(term, frequencies);
        }

        // get the Term Frequency (TF) = (Number of times term t appears in a document)
        // (Total number of terms in the document).
        Map<String, Map<Integer, Double>> normalizedTermFrequencies = new HashMap<>();
        for (String term : invertedIndex.getTerms()) {
            Map<Integer, Double> frequencies = new HashMap<>();
            for (int docId = 1; docId <= LengthFilenames; docId++) {
                int frequency = termFrequencies.get(term).get(docId);
                int length = documentLengths.get(docId);// cumulative term frequency of all terms in the specified
                                                        // document# of words for doc
                double normalizedFrequency = frequency / (double) length;
                frequencies.put(docId, normalizedFrequency);
            }
            normalizedTermFrequencies.put(term, frequencies);
        }

        // get the Inverse Document Frequency (IDF)
        Map<String, Double> inverseDocumentFrequencies = new HashMap<>();
        for (String term : invertedIndex.getTerms()) {
            double IDF = invertedIndex.getIDF(term);
            inverseDocumentFrequencies.put(term, IDF);
        }

        // get the TF-IDF
        Map<String, Map<Integer, String>> TF_IDF = new HashMap<>();
        for (String term : invertedIndex.getTerms()) {
            Map<Integer, String> TF_IDF_scores = new HashMap<>();
            for (int docId = 1; docId <= LengthFilenames; docId++) {
                double TF = normalizedTermFrequencies.get(term).get(docId);
                double IDF = inverseDocumentFrequencies.get(term);
                // double is 3 number after point if zero must be 3 number after point
                DecimalFormat df = new DecimalFormat("0.000");
                String TF_IDF_score = df.format(TF * IDF);
                TF_IDF_scores.put(docId, TF_IDF_score);
            }
            TF_IDF.put(term, TF_IDF_scores);
        }
        return TF_IDF;

    }

}

class Posting {
    int dtf;
    Map<Integer, Integer> positions;
    Posting next;

    public Posting(int docId, int position) {
        dtf = 1;
        positions = new HashMap<>();
        positions.put(docId, position);
        next = null;
    }
}

class DictEntry {
    int doc_freq;
    int term_freq;
    Map<Integer, List<Integer>> postings;

    public DictEntry() {
        doc_freq = 0;
        term_freq = 0;
        postings = new HashMap<>();
    }

    public void addPosting(int docId, int position) {
        postings.putIfAbsent(docId, new ArrayList<>());
        postings.get(docId).add(position);
    }
}

class InvertedIndex {
    private Map<String, DictEntry> index;
    private int NumOfDocs;

    public InvertedIndex() {
        index = new HashMap<>();
        NumOfDocs = 10;
    }

    public void addTerm(String term, int docId, int position) {
        index.putIfAbsent(term, new DictEntry()); // add term to the index if it doesn't exist
        DictEntry entry = index.get(term); // get the DictEntry for the term
        entry.term_freq++; // increment term frequency
        entry.addPosting(docId, position); // add posting for the term

        // increment document frequency if this is the first occurrence of the term in
        // the document
        if (entry.postings.get(docId).size() == 1) {
            entry.doc_freq++;
        }
    }

    public int getDocumentWordsLength(int docId) {
        int length = 0;
        for (DictEntry entry : index.values()) {
            if (entry.postings.containsKey(docId)) {
                length += entry.postings.get(docId).size();
            }
        }
        return length;
    }

    // IDF(t) = log(Total number of documents / Number of documents with term t in
    // it).
    public double getIDF(String term) {
        if (!index.containsKey(term)) {
            return 0;
        }
        int docFreq = index.get(term).doc_freq;
        return Math.log10(NumOfDocs / (double) docFreq);
    }

    public int getTFInDocument(String term, int docId) {
        DictEntry entry = index.get(term);
        if (entry != null && entry.postings.containsKey(docId)) {
            return entry.postings.get(docId).size();
        }
        return 0;
    }

    public int getTFInQuery(String term, String[] queryTerms) {
        int frequency = 0;
        for (String queryTerm : queryTerms) {
            if (queryTerm.equals(term)) {
                frequency++;
            }
        }
        return frequency;
    }

    public double getDocumentVectorLength(int docId) {
        double length = 0.0;
        for (DictEntry entry : index.values()) {
            if (entry.postings.containsKey(docId)) {
                int termFrequency = entry.postings.get(docId).size();
                length += Math.pow(termFrequency, 2);
            }
        }
        return Math.sqrt(length);
    }

    public double getQueryVectorLength(String[] queryTerms) {
        double length = 0.0;
        for (String term : queryTerms) {
            int termFrequency = getTFInQuery(term, queryTerms);
            length += Math.pow(termFrequency, 2);
        }
        return Math.sqrt(length);
    }

    public Set<String> getTerms() {
        return index.keySet();
    }

}