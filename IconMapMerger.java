import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class IconMapMerger
{
    public static void main(String[] args)
    {
        String[] paths = new String[]
        {
            "IconMapSurvival.xml"
        };

        new IconMapMerger(paths);
    }

    private final String base = "";

    private final String[] header = new String[]
    {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
        "    <MyGUI type=\"Resource\" version=\"1.1\">",
        "        <Resource type=\"ResourceImageSet\" name=\"ItemIconsSetSurvival0\">",
        "            <Group name=\"ItemIconsSurvival\" texture=\"IconMapSurvival.png\" size=\"96 96\">"
    };

    private final String[] end = new String[]
    {
        "       </Group>",
        "   </Resource>",
        "</MyGUI>"
    };

    private int max_width = 0;
    private int max_height = 0;
    private ArrayList<Icon> icons = new ArrayList<Icon>();

    public IconMapMerger(String[] paths)
    {
        try
        {
            for(String path : paths)
            {
                loadMap(path);
            }
            filter();
            merge();
        }
        catch (Exception e){e.printStackTrace();}
    }

    private void loadMap(String map_path) throws IOException
    {
        File map_file = new File("Source/" + map_path);
        if(!map_file.exists()) throw new IllegalArgumentException("Map file doesnt exists!");

        String content = "";
        Scanner input = new Scanner(map_file);
        while(input.hasNextLine())
        {
            content += input.nextLine().trim();
        }
        String[] parts = content.replaceAll("><", ">\n<").split("\n");
        int width = 0;
        int height = 0;
        BufferedImage texture = null;
        for(int i = 0; i < parts.length; i++)
        {
            if(parts[i].matches("<Group.+"))
            {
                String image_path = parts[i].replaceFirst("^.+texture=\"", "").replaceFirst("\".+", "");
                File image_file = new File("Source/" + image_path);
                if(!image_file.exists()) throw new IllegalArgumentException("Image file doesnt exists!");
                texture = ImageIO.read(image_file);
                String size = parts[i].replaceFirst("^.+size=\"", "").replaceFirst("\".+", "");
                String[] dimensions = size.split(" ");
                if(dimensions.length != 2) throw new IllegalArgumentException("Icon size error!");
                if(!dimensions[0].matches("^\\d+$")) throw new IllegalArgumentException("Icon size error!");
                if(!dimensions[1].matches("^\\d+$")) throw new IllegalArgumentException("Icon size error!");
                width = Integer.parseInt(dimensions[0]);
                height = Integer.parseInt(dimensions[1]);
                this.max_height = Math.max(this.max_height, height);
                this.max_width = Math.max(this.max_width, width);
                if(width == 0 || height == 0) throw new IllegalArgumentException("Icon size error!");
            }
            else if(parts[i].matches("<Index.+"))
            {
                if(texture == null)  throw new IllegalArgumentException("No image file specified!");
                if(i + 1 >= parts.length) throw new IllegalArgumentException("Mapping error!");
                String nextLine = parts[i + 1];
                if(!nextLine.matches("^<Frame.+")) throw new IllegalArgumentException("Mapping error!");
                String uuid = parts[i].replaceFirst("^.+name=\"", "").replaceFirst("\".+", "");
                String position = nextLine.replaceFirst("^.+point=\"", "").replaceFirst("\".+", "");
                String[] dimensions = position.split(" ");
                if(dimensions.length != 2) throw new IllegalArgumentException("Icon position error!");
                if(!dimensions[0].matches("^\\d+$")) throw new IllegalArgumentException("Icon position error!");
                if(!dimensions[1].matches("^\\d+$")) throw new IllegalArgumentException("Icon position error!");
                int x = Integer.parseInt(dimensions[0]);
                int y = Integer.parseInt(dimensions[1]);
                this.icons.add(new Icon(uuid, texture.getSubimage(x, y, width, height)));
            }
        }
    }

    private void filter()
    {
        int counter = 0;
        for(int i = 0; i < this.icons.size(); i++)
        {
            String uuid = this.icons.get(i).getUuid();
            ArrayList<Integer> duplicates = new ArrayList<Integer>();
            for(int k = i + 1; k < this.icons.size(); k++)
            {
                if(uuid.equals(this.icons.get(k).getUuid()))
                {
                    duplicates.add(k);
                }
            }

            for(int k = duplicates.size() - 1; k >= 0; k--)
            {
                this.icons.remove((int) duplicates.get(k));
            }
        }
    }

    private void merge() throws IOException
    {
        int root = (int) Math.ceil(Math.sqrt(this.icons.size()));
        BufferedImage image = new BufferedImage(root * this.max_width, root * this.max_height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) image.createGraphics();

        String content = "";
        for(String line : this.header)
        {
            content += line + "\n";
        }

        for(int i = 0; i < this.icons.size(); i++)
        {
            Icon icon = this.icons.get(i);
            int x = (i % root) * this.max_width;
            int y = (int)Math.floor(i / (double) root) * this.max_height;
            content += "\t\t\t<Index name=\"" + icon.getUuid() + "\">\n";
            content += "\t\t\t\t<Frame point=\"" + x + " " + y + "\"/>\n";
            content += "\t\t\t</Index>\n";
            g.drawImage(icon.getImage(), x, y, null);
        }

        for(String line : this.end)
        {
            content += line + "\n";
        }

        PrintWriter writer = new PrintWriter(new File("Result/IconMapSurvival.xml"));
        writer.println(content);
        writer.close();

        ImageIO.write(image, "png", new File("Result/IconMapSurvival.png"));
    }


}
