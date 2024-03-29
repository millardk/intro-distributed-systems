package cs455.hadoop.part1;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Part1AnalysisValue implements Writable {

    private DoubleWritable hotttnesss = new DoubleWritable();
    private DoubleWritable danceability = new DoubleWritable();
    private DoubleWritable duration = new DoubleWritable();
    private DoubleWritable endFadeIn = new DoubleWritable();
    private DoubleWritable energy = new DoubleWritable();
    private DoubleWritable loudness = new DoubleWritable();

    public Part1AnalysisValue(){}

    @Override
    public void write(DataOutput out) throws IOException {
        hotttnesss.write(out);
        danceability.write(out);
        duration.write(out);
        endFadeIn.write(out);
        energy.write(out);
        loudness.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        hotttnesss.readFields(in);
        danceability.readFields(in);
        duration.readFields(in);
        endFadeIn.readFields(in);
        energy.readFields(in);
        loudness.readFields(in);
    }

    public double getHotttnesss() {
        return hotttnesss.get();
    }

    public double getDanceability() {
        return danceability.get();
    }

    public double getDuration() {
        return duration.get();
    }

    public double getEnergy() {
        return energy.get();
    }

    public double getEndFadeIn() {
        return endFadeIn.get();
    }

    public double getLoudness() {
        return loudness.get();
    }

    public Part1AnalysisValue setHotttnesss(double hotttnesss) {
        this.hotttnesss.set(hotttnesss);
        return this;
    }

    public Part1AnalysisValue setDanceability(double danceability) {
        this.danceability.set(danceability);
        return this;
    }

    public Part1AnalysisValue setDuration(double duration) {
        this.duration.set(duration);
        return this;
    }

    public Part1AnalysisValue setEndFadeIn(double endFadeIn) {
        this.endFadeIn.set(endFadeIn);
        return this;
    }

    public Part1AnalysisValue setEnergy(double energy) {
        this.energy.set(energy);
        return this;
    }

    public Part1AnalysisValue setLoudness(double loudness) {
        this.loudness.set(loudness);
        return this;
    }

    @Override
    public String toString() {
        return hotttnesss+" "
                +danceability+" "
                +duration+" "
                +endFadeIn+" "
                +energy+" "
                +loudness;
    }

}
