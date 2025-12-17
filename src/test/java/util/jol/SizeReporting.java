package util.jol;

import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import com.datadoghq.sketch.ddsketch.DDSketch;
import com.datadoghq.sketch.ddsketch.DDSketches;
import com.xceptance.xlt.report.util.RuntimeHistogram;
import com.xceptance.xlt.report.util.rework.IntTimeSeries;
import com.xceptance.xlt.report.util.rework.IntTimeSeriesEntry;
import com.xceptance.xlt.report.util.rework.ReservoirSampling;

//@Ignore("Utility class to report object sizes using JOL, for debugging and optimization purposes")
public class SizeReporting
{
    @Test
    public void jolIntTimeSeriesClass()
    {
        System.out.println(VM.current().details());
        System.out.println("==========================================================================================");
        System.out.println(ClassLayout.parseClass(IntTimeSeries.class).toPrintable());    
        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(new IntTimeSeries(900)).toFootprint());
    }

    @Test
    public void jolIntTimeSeriesEntryInstance()
    {
        System.out.println(VM.current().details());
        System.out.println("==========================================================================================");
        System.out.println(ClassLayout.parseClass(IntTimeSeriesEntry.class).toPrintable());    
        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(new IntTimeSeriesEntry()).toFootprint());
    }
    
    @Test
    public void jolRuntimeHistogram()
    {
        System.out.println(VM.current().details());
        System.out.println("==========================================================================================");
        System.out.println(ClassLayout.parseClass(RuntimeHistogram.class).toPrintable());    
        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(new RuntimeHistogram(10)).toFootprint());
        
        var histogram = new RuntimeHistogram(10);
        for (int i = 0; i < 1000; i++)
        {
            histogram.addValue(i);
        }
        for (int i = 8000; i < 10000; i++)
        {
            histogram.addValue(i);
        }
        for (int i = 29000; i < 30000; i++)
        {
            histogram.addValue(i);
        }
        histogram.addValue(45000);

        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(histogram).toFootprint());
    }
    
    @Test
    public void jolReserviorSampling()
    {
        System.out.println(VM.current().details());
        System.out.println("==========================================================================================");
        System.out.println(ClassLayout.parseClass(ReservoirSampling.class).toPrintable());    
        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(new ReservoirSampling(1000)).toFootprint());
        
        var histogram = new ReservoirSampling(1000);
        for (int i = 0; i < 1000; i++)
        {
            histogram.add(i);
        }
        for (int i = 8000; i < 10000; i++)
        {
            histogram.add(i);
        }
        for (int i = 29000; i < 30000; i++)
        {
            histogram.add(i);
        }
        histogram.add(45000);

        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(histogram).toFootprint());
    }
    
    @Test
    public void jolDDSketch()
    {
        System.out.println(VM.current().details());
        System.out.println("==========================================================================================");
        System.out.println(ClassLayout.parseClass(DDSketch.class).toPrintable());    
        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(DDSketches.unboundedDense(0.01)).toFootprint());
        
        var histogram = DDSketches.unboundedDense(0.01);
        for (int i = 0; i < 1000; i++)
        {
            histogram.accept(i);
        }
        for (int i = 8000; i < 10000; i++)
        {
            histogram.accept(i);
        }
        for (int i = 29000; i < 30000; i++)
        {
            histogram.accept(i);
        }
        histogram.accept(45000);

        System.out.println("==========================================================================================");
        System.out.println(GraphLayout.parseInstance(histogram).toFootprint());
    }
}
