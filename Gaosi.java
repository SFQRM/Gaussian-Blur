import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;   
  
public class Gaosi
{
    final static int radius = 1;//高斯模糊半径
    final static int  size = 2*radius+1;//矩阵大小    
    static float sum=0;//高斯权重矩阵的和
    static float [][]weightMatrix;//计算高斯后的权重矩阵
 
 /**
     * 简单高斯模糊算法
     * 
     * 所谓"模糊"，可以理解成每一个像素都取周边像素的平均值。即，中间点失去细节。
     * 显然，计算平均值时，取值范围越大，"模糊效果"越强烈。
     * 接下来的问题就是，如何取周围点的平均值。
     * 因为图像的每个像素点都是连续的，距离越近的像素点关系越密切，越远的像素点关系越疏远。
     * 如果使用简单平均，这显然不合理。
     * 因此，加权平均更合理，距离越近的点权重越大，距离越远的点权重越小。
     * 因为正态分布是一种钟形曲线，越接近中心，取值越大，越远离中心，取值越小。
     * 所以我们在这里使用正态分布的权重。
     * 计算平均值的时候，我们只需要将"中心点"作为原点，其他点按照其在正态曲线上的位置，
     * 分配权重，就可以得到一个加权平均值。
     * 正态分布是一维的，图像都是二维的，所以我们需要二维的正态分布。
     * 二维高斯函数：e^(-(x^2+y^2)/2*σ^2)/2*PI*σ^2
     * 为了计算权重矩阵，需要假定中心点的坐标是（0,0），设定σ的值。假定σ=2.0，
     * 这9个点的权重总和等于sum，如果只计算这9个点的加权平均，还必须让它们的权重之和等于1，
     * 因此上面9个值还要分别除以sum，得到最终的权重矩阵。
     * 对图片每个像素点的RGB三个通道分别、反复做高斯模糊。
     */
    public static void main(String[] args) throws IOException
    {    
        weightMatrix = kernalDataOf2D(radius, 2.0f);//计算高斯权重
        BufferedImage img = ImageIO.read(new File("d:\\lena.jpg"));
        System.out.println("图片加载成功" + img);
        int height = img.getHeight();
        int width = img.getWidth();   
       
        int[][] matrix = new int[size][size];//基础矩阵
        int[] values = new int[size*size];//像素点RGB信息值
        
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                readPixel(img, i, j, values);//获取周边点的RGB信息值
                fillMatrix(matrix, values);//将周边点个点的RGB信息值存到缓存矩阵中               
                img.setRGB(i, j, resetMatrix(matrix));
            }
        }
        ImageIO.write(img, "jpeg", new File("d:/result.jpg"));//保存在d盘为result.jpg文件
    }
     
    
    //计算高斯权重矩阵
    public static float[][] kernalDataOf2D(int n, float sigma) 
    {  
        int size = 2*n +1;  
        float sigma22 = 2*sigma*sigma;  
        float sigma22PI = (float)Math.PI * sigma22;  
        float[][] kernalData = new float[size][size];  
        int row = 0;		//行数
        
        for(int i=-n; i<=n; i++) 
        {  
            int col = 0;  			//列数
            for(int j=-n; j<=n; j++) 
            {  
                float xDistance = i*i;  
                float yDistance = j*j;  
                kernalData[row][col] = (float)Math.exp(-(xDistance + yDistance)/sigma22)/sigma22PI;  
                col++;  
            }  
            row++;  
        }  
        System.out.println("二维高斯矩阵结果："); 
        for(int i=0; i<size; i++) 
        {  
            for(int j=0; j<size; j++) 
            {  
                sum +=kernalData[i][j];									//计算矩阵的和，方便后面的平均化
                System.out.print("\t" + kernalData[i][j]);  
            }  
            System.out.println();  
            System.out.println("\t ---------------------------------------------");  
        }  
        //均值化高斯权重矩阵
        /**
           * 这9个点的权重总和等于sum，如果只计算这9个点的加权平均，还必须让它们的权重之和等于1，
           * 因此上面9个值还要分别除以sum，得到最终的权重矩阵。
           */
        System.out.println("均值后：");
        for(int i=0; i<kernalData.length; i++) 
        {  
           for(int j=0; j<kernalData.length; j++) 
           {  
               kernalData[i][j] = kernalData[i][j]/sum;
               System.out.print("\t" + kernalData[i][j]);  
           }  
           System.out.println();  
           System.out.println("\t ---------------------------------------------"); 
       }
        return kernalData;  
     }
    
    //读取像素RGB信息
    private static void readPixel(BufferedImage img, int x, int y, int[] pixels)
    {
    	int current = 0;
    	int xStart = x - radius;
        int yStart = y - radius;
        //对该点的左边点和右边点进行边界检查
        for (int i = xStart; i < size + xStart; i++)
            for (int j = yStart; j < size + yStart; j++)
            {
                int pixelX = i;
                if (pixelX < 0)//处理边界情况左溢出
                    pixelX = -pixelX;
                else if (pixelX >= img.getWidth())//处理边界情况右溢出
                    pixelX = x;
                  
                int pixelY = j;
                if (pixelY < 0)
                    pixelY = -pixelY;
                else if (pixelY >= img.getHeight())
                    pixelY = y;
               
                pixels[current] = img.getRGB(pixelX, pixelY);//获取该点的RGB数据
                current++;
            }
    }
      
    //将周边点个点的RGB信息值存到缓存矩阵中
    private static void fillMatrix(int[][] matrix, int[] values)
    {
        int count = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j <size; j++)
            {            
                matrix[i][j] = values[count];
                count++;
            }
    }
     
    //重置基本矩阵，将该点的RGB信息值与高斯权重矩阵相乘
    private static int resetMatrix(int[][] matrix)
    {    
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j <matrix.length; j++)
            {
                Color c = new Color(matrix[i][j]);            
                r += c.getRed()*weightMatrix[i][j];              
                g += c.getGreen()*weightMatrix[i][j];
                b += c.getBlue()*weightMatrix[i][j];
            }
        return new Color(r, g, b).getRGB();
    }
    
}
 
