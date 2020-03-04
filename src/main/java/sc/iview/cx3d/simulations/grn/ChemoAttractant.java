package sc.iview.cx3d.simulations.grn;

import cleargl.GLVector;
import graphics.scenery.volumes.TransferFunction;
import graphics.scenery.volumes.bdv.Volume;
import ij.IJ;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChemoAttractant {
    // Singleton factory
    protected static Map<String, Object> factory = initialFactory();

    private static Map<String, Object> initialFactory() {
        Map<String, Object> f = new HashMap<>();
        f.put("color", new float[]{1, 1, 1});// HSB
        f.put("name", "A");
        return f;
    }

    // Fields
    private double maxConcentration = 1;
    private double coordinate = 300;
    private int dimension = 0;
    private double sigma = 160.0;
    private float[] color = new float[]{1, 1, 1};
    private String substanceName;
    private Substance substance;
    private RandomAccessible<FloatType> concentrationImg;
    private Interval interval;

    /** Create a FunctionRandomAccessible of a gaussianConcentration */
    public static final FunctionRandomAccessible<FloatType> gaussianConcentration(
			final int dimension,
			final double coordinate,
			final double scale,
			final double stretch) {
		return new FunctionRandomAccessible<>(
                3,
                (location, value) -> {
                    final double v = location.getDoublePosition(dimension);
                    value.set(
                            (float) (scale / (stretch * Math.abs(v - coordinate) + 1)));
                },
                FloatType::new);
	}

	/** Singleton factory method to create a Gaussian attractor Substance in the ECM */
	public static ChemoAttractant createGaussianAttractor(ECM ecm, int dimension, double coordinate) {
        ChemoAttractant ca = new ChemoAttractant();
        ca.maxConcentration = 1;
        ca.coordinate = coordinate;
        ca.dimension = dimension;
        ca.sigma = 160;
        ca.color = ChemoAttractant.getNextColor();
        ca.substanceName = ChemoAttractant.getNextName();

        Substance s = new Substance(ca.substanceName, Color.getHSBColor(ca.color[0], ca.color[1], ca.color[2]));
        if( ca.dimension == 2 ) {
            ecm.addArtificialGaussianConcentrationZ(s, ca.maxConcentration, ca.coordinate, ca.sigma);
        } else if ( ca.dimension == 0 ) {
            ecm.addArtificialGaussianConcentrationX(s, ca.maxConcentration, ca.coordinate, ca.sigma);
        } else if ( ca.dimension == 1 ) {
            ecm.addArtificialGaussianConcentrationY(s, ca.maxConcentration, ca.coordinate, ca.sigma);
        }
		ca.substance = s;

		return ca;
    }

    /** Singleton factory method to create a Gaussian attractor Substance in the ECM */
	public static ChemoAttractant createGaussianImgAttractor(ECM ecm, int dimension, double coordinate, boolean showVolume) {
        ChemoAttractant ca = new ChemoAttractant();
        ca.maxConcentration = 1;
        ca.coordinate = coordinate;
        ca.dimension = dimension;
        ca.sigma = 50;
        ca.color = ChemoAttractant.getNextColor();
        ca.substanceName = ChemoAttractant.getNextName();

        Substance s = new Substance(ca.substanceName, Color.getHSBColor(ca.color[0], ca.color[1], ca.color[2]));

		long[] max = new long[]{500, 500, 500};
		//long[] max = new long[]{1000, 1000, 1000};
		long[] min = new long[]{0, 0, 0};
		//long[] min = new long[]{-500, -500, -500};

		FunctionRandomAccessible<FloatType> conc = gaussianConcentration(ca.dimension, ca.coordinate, 1, 1);
		RandomAccessibleInterval<FloatType> concInterval = Views.interval(conc, new FinalInterval(min, max));

		RandomAccessibleInterval<UnsignedByteType> volImg = Converters.convert(concInterval, (a, b) -> b.set((int)(255 * a.getRealDouble())), new UnsignedByteType());

		System.out.println("Volume is : " + volImg.dimension(0) + " " + volImg.dimension(1) + " " + volImg.dimension(2));

		double transformScale = 1.0;

		if( ECM.isSciviewEnabled() && showVolume ) {
		    // Now make a render volume that is smaller and display a smaller version
            RandomAccessibleInterval<UnsignedByteType> renderImg =
                Views.interval(
                    RealViews.affine(
                            Views.interpolate(
                                    Views.extendZero(volImg),
                                    new NearestNeighborInterpolatorFactory<>()),
                                    //new NLinearInterpolatorFactory<>()),
                            new Scale3D(transformScale, transformScale, transformScale)),
		        new FinalInterval(
		            new long[]{0, 0, 0},
                    new long[]{500, 500, 500}
                ));

            IJ.saveAsTiff(ImageJFunctions.wrap(renderImg,"test"), "/tmp/testVolume.tif");

            //Volume vol = (Volume) ecm.getSciViewCX3D().getSciView().addVolume(volImg, "circuit", new float[]{1, 1, 1});
            Volume vol = (Volume) ecm.getSciViewCX3D().getSciView().addVolume(renderImg, "circuit", new float[]{1, 1, 1});

            //vol.setTransferFunction(TransferFunction.ramp(0.0F, 0.38F));
            vol.setScale(new GLVector((float) transformScale, (float) transformScale, (float) transformScale).times(2));

            vol.updateWorld(true, true);
        }

		//long[] offset = new long[]{(long) (concInterval.dimension(0) * -0.5), (long) (concInterval.dimension(1) * -0.5), (long) (concInterval.dimension(2) * -0.5)};

		//final Scale3D transformScale3D = new Scale3D(transformScale, transformScale, transformScale);
		final Scale3D transformScale3D = new Scale3D(1, 1, 1);
		RandomAccessible<FloatType> concentrationImg =
				RealViews.affine(
						Views.interpolate(
								//Views.extendZero(Views.translate(concInterval, offset)),
                                Views.extendZero(concInterval),
								new NearestNeighborInterpolatorFactory<>()),
								//new NLinearInterpolatorFactory<>()),
						transformScale3D);

		ca.setConcentrationImg(concentrationImg);
		ca.setInterval(concInterval);
		//ca.setInterval(Views.translate(concInterval, offset));

		System.out.println("Adding Img Concentration");
        ecm.addArtificialImgConcentration( s.getId(), concentrationImg);

		return ca;
    }

    private void setInterval(Interval interval) {
	    this.interval = interval;
    }

    private void setConcentrationImg(RandomAccessible<FloatType> concentrationImg) {
	    this.concentrationImg = concentrationImg;
    }

    private static String getNextName() {
        String currName = (String) factory.get("name");
        String thisName = currName;
        char c = currName.charAt(currName.length()-1);
        thisName.replace(c, (char) (c+1));
        factory.put("name", thisName);
        return thisName;
    }

    /** Return the next available color.
     * Note: this method references variables used in getNextName */
    private static float[] getNextColor() {
        float[] currColor = (float[]) factory.get("color");
        float[] thisColor = Arrays.copyOf(currColor, 3);
        String currName = (String) factory.get("name");
        int diff =  currName.charAt(currName.length()-1) - Character.getNumericValue('A');
        thisColor[diff % 3] -= 0.15;
        factory.put("color", Arrays.copyOf(thisColor, 3));
        return thisColor;
    }

    public RandomAccessible<FloatType> getConcentrationImg() {
        return concentrationImg;
    }

    public Interval getInterval() {
        return interval;
    }
}
