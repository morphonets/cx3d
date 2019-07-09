package ini.cx3d.physics;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class ImgSubstance extends Substance {
    private Img<FloatType> img;

    public void setImg( Img<FloatType> img ) {
        this.img = img;
    }
}
