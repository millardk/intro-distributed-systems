package cs455.hadoop.q9;

import cs455.hadoop.io.*;
import cs455.hadoop.util.TopList;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.IOException;
import java.util.*;

public class Q9Reducer extends Reducer<CustomWritableComparable, CustomWritable, Text, Text> {

    SimpleRegression[] regressions = new SimpleRegression[9];
    HashMap<Text, SimpleRegression> termRegressions = new HashMap<>();
    ArrayList<ArtistHotttnesss> artistList = new ArrayList<>();
    SimpleRegression artistNameLength = new SimpleRegression();
    SimpleRegression artistNameWords = new SimpleRegression();

    public Q9Reducer() {
        for (int i = 1; i < 9; i++) {
            regressions[i] = new SimpleRegression();
        }
    }

    @Override
    protected void reduce(CustomWritableComparable key, Iterable<CustomWritable> values, Context context) throws IOException, InterruptedException {

        Artist artist = null;
        DoubleArrayWritable array = null;

        Iterator<CustomWritable> it = values.iterator();
        while (it.hasNext()) {
            CustomWritable customWritable = it.next();
            if (customWritable.getId() == CustomWritable.ARTIST)
                artist = (Artist) customWritable.getInner();

            else if (customWritable.getId() == CustomWritable.DOUBLE_ARRAY)
                array = (DoubleArrayWritable) customWritable.getInner();

        }

        if (artist == null || array == null)
            return;

        DoubleWritable[] dwArray = array.toArray();
        double hotttnesss = dwArray[0].get();
        for (int i = 1; i < 9; i++) {
            regressions[i].addData(hotttnesss, dwArray[i].get());
        }

        ArtistTerm[] terms = artist.terms.toArray();
        for(ArtistTerm term : terms)
            termRegressions.putIfAbsent(term.term, new SimpleRegression());

        String artistName = artist.artistName.toString();

        artistNameLength.addData(hotttnesss, artistName.length());
        artistNameWords.addData(hotttnesss, artistName.split(" ").length);

        artistList.add(new ArtistHotttnesss(hotttnesss, terms));

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {

        final double hotttnessTarget = 1.2;
        context.write(new Text("attribute significance slope prediction"), new Text(String.format("hotttnesssTarget %.1f", hotttnessTarget)));

        for(int i = 1; i < 9; i++)
        {
            SimpleRegression regression = regressions[i];
            double significance = regression.getSignificance();
            double slope = regression.getSlope();
            double predict = regression.predict(hotttnessTarget);

            context.write(new Text(Q9AnalysisMapper.arrayValues[i]), new Text(String.format("%.2f %.2f %.2f", significance,  slope, predict)));
        }

        TopList<TermRegression> topTerms = new TopList<>(10, Comparator.comparingDouble(o -> o.predict));

        for (ArtistHotttnesss artistHotttnesss : artistList) {
            Set<Text> termsVisited = termRegressions.keySet();

            for (ArtistTerm term : artistHotttnesss.terms) {
                SimpleRegression regression = termRegressions.get(term.term);
                regression.addData(artistHotttnesss.hotttnesss, term.weight);
                termsVisited.remove(term);
            }

            for (Text term :termsVisited) {
                SimpleRegression regression = termRegressions.get(term);
                regression.addData(artistHotttnesss.hotttnesss, 0);
            }
        }

        for (Text term : termRegressions.keySet()) {
            TermRegression tr = new TermRegression(term, termRegressions.get(term).predict(hotttnessTarget));
            topTerms.addIfTop(tr);
        }

        {
            double significance = artistNameLength.getSignificance();
            double slope = artistNameLength.getSlope();
            double predict = artistNameLength.predict(hotttnessTarget);
            context.write(new Text("Artist Name Length:"), new Text(String.format("%.2f %.2f %.2f", significance,  slope, predict)));
        }

        {
            double significance = artistNameWords.getSignificance();
            double slope = artistNameWords.getSlope();
            double predict = artistNameWords.predict(hotttnessTarget);
            context.write(new Text("Artist Name Words:"), new Text(String.format("%.2f %.2f %.2f", significance,  slope, predict)));
        }

        context.write(new Text("Top Terms:"), new Text("\n"+topTerms.toString()));
    }

}

class TermRegression {
    Text term;
    double predict;

    public TermRegression(Text term, double predict) {
        this.term = term;
        this.predict = predict;
    }

    @Override
    public String toString() {
        return term.toString()+" "+String.format("%.2f", predict);
    }
}

class ArtistHotttnesss {

    double hotttnesss;
    ArtistTerm[] terms;

    public ArtistHotttnesss(double hotttnesss, ArtistTerm[] terms) {
        this.hotttnesss = hotttnesss;
        this.terms = terms;
    }

}
