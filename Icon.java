import java.awt.image.BufferedImage;

public class Icon
{
    private String uuid;
    private BufferedImage image;

    public Icon(String uuid, BufferedImage image)
    {
        this.uuid = uuid;
        this.image = image;
    }

    public BufferedImage getImage()
    {
        return this.image;
    }

    public String getUuid()
    {
        return this.uuid;
    }
}
