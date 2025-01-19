package henson.games;

import com.nokia.mid.ui.*;
import henson.midp.*;
import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class MainCanvas extends FullCanvas
{
  public World world=null;
  public long startTime=System.currentTimeMillis();
  private MoonLight ml=null;
  private Random rand=new Random();
  private int[] land=new int[100];
  private Image o2=null;
  private boolean o2_visible=true;
  private Image fimg=null;
  private boolean fimg_visible=true;
  private boolean takeoff_visible=true;
  private boolean thisway_visible=true;
  private Image bufferImage=null;
  /**Construct the displayable*/
  public MainCanvas(MoonLight ml)
  {
    this.ml=ml;
    for(int i=0; i<100; i++)
      land[i]=rand.nextInt()&3;
    bufferImage = Image.createImage(getWidth(), getHeight());
  }

  protected void keyPressed( int keyCode )
  {
    switch(keyCode)
    {
      case KEY_SOFTKEY2:
        if(ml.landing || ml.lostControl)
        {
          long curTime=(System.currentTimeMillis()-startTime)/1000;
          ml.flights++;
          ml.flytime+=curTime;
          ml.oxygen-=curTime/60;
          ml.setDisplay(6);
        }
        break;
      case KEY_NUM4:
        if(world!=null && ml.lostControl==false)
        {
          if(world.angle>-70 && world.y.Great(Float.ZERO))
            world.angle-=5;
        }
        break;
      case KEY_NUM6:
        if(world!=null && ml.lostControl==false)
        {
          if(world.angle<70 && world.y.Great(Float.ZERO))
            world.angle+=5;
        }
        break;
      case KEY_NUM8:
        if(world!=null && ml.lostControl==false)
        {
          if(world.power>0)
            world.power-=10;
        }
        break;
      case KEY_NUM2:
        if(world!=null && ml.lostControl==false)
        {
          if(world.power<100)
            world.power+=10;
        }
        break;
    }
  }

  /** Required paint implementation */
  protected void paint(Graphics g2)
  {
    Graphics g = bufferImage.getGraphics();
    g.setColor( 255, 255, 255 );
    g.fillRect( 0, 0, getWidth(), getHeight() );
    g.setColor( 0, 0, 0 );
    /** @todo Add paint codes */
    if(world==null)
    {
      g.drawString("World not found",0,0,0);
      return;
    }
    //
    int xcenter=getWidth()/2;
    int ycenter=getHeight()/2;
    //
    Font font=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int y=font.getHeight()+1;
    g.setFont(font);
    long curTime=(System.currentTimeMillis()-startTime)/1000;
    g.drawString( "T:"+curTime, y, 0, g.TOP | g.LEFT );
    g.drawString( "P:"+world.power+"%", getWidth(), 0, g.TOP | g.RIGHT );
    g.drawString( "A:"+world.angle, getWidth(), getHeight(), g.BOTTOM | g.RIGHT );
    g.drawString( "D:"+world.x.toLong(), xcenter, getHeight(), g.BOTTOM | g.HCENTER );
    g.drawString( "H:"+world.y.toLong(), 0, ycenter, g.TOP | g.LEFT );
    g.drawString( "F:"+ml.fuel.toLong(), 0, getHeight(), g.BOTTOM | g.LEFT );
    //
    int yLand=getHeight()-y-1;
    //
    int ycenter2=yLand-3-(int)world.y.Div(10L).toLong();
    // Условие, чтобы всегда была видна поверхность
    if(ycenter2<20)
    {
      yLand+=20-ycenter2;
      ycenter2=20;
    }
    //
    g.setStrokeStyle(Graphics.DOTTED);
    g.drawLine(xcenter, ycenter2, xcenter+20, ycenter2);
    g.drawLine(xcenter, ycenter2, xcenter, ycenter2-20);
    g.setStrokeStyle(Graphics.SOLID);
    //
    int xp[]={ 4, 12, 0, 8, 16, 0, 16, 8, 5, 8, 11, 19, 19, -3, -3 };
    int yp[]={ 0, 0, 3, 3, 3, 6, 6, 6, 9, 6+world.power/10, 9, 2, 8, 2, 8 };
    //
    for(int i=0; i<15; i++)
    {
      xp[i]+=xcenter-8;
      yp[i]+=ycenter2-5;
    }
    //
    world.rotatePoints(xcenter, ycenter2, xp, yp, 15);
    //
    g.drawLine(xp[2],yp[2],xp[4],yp[4]);
    g.drawLine(xp[2],yp[2],xp[5],yp[5]);
    g.drawLine(xp[5],yp[5],xp[6],yp[6]);
    g.drawLine(xp[6],yp[6],xp[4],yp[4]);
    //
    g.drawLine(xp[2],yp[2],xp[13],yp[13]);
    g.drawLine(xp[13],yp[13],xp[14],yp[14]);
    //
    g.drawLine(xp[4],yp[4],xp[11],yp[11]);
    g.drawLine(xp[11],yp[11],xp[12],yp[12]);
    //
    g.drawArc(xcenter-8, ycenter2-8, 16, 16, -world.angle, 180);
    //
    DirectGraphics dg = DirectUtils.getDirectGraphics(g);
    dg.fillPolygon(xp, 7, yp, 7, 4, 0xFF000000);
    //
    g.drawString( ""+world.Vx.toLong(), xcenter+20, ycenter2, g.LEFT | g.BOTTOM );
    g.drawString( ""+world.Vy.toLong(), xcenter, ycenter2-20, g.RIGHT | g.TOP );
    // Ground objects
    Float diff=world.x;
    // Стартовая точка рисуем палки по бокам
    int s=xcenter-(int)diff.Div(10L).toLong()-15;
    if(s>0 && s<getWidth())
    {
      g.drawLine(s,yLand,s,yLand-10);
      //
      g.drawLine(s-2,yLand-7,s,yLand-10);
      g.drawLine(s+2,yLand-7,s,yLand-10);
    }
    if(s+30>0 && s+30<getWidth())
    {
      g.drawLine(s+30,yLand,s+30,yLand-10);
      //
      g.drawLine(s+28,yLand-7,s+30,yLand-10);
      g.drawLine(s+32,yLand-7,s+30,yLand-10);
    }
    // Конечная точка рисуем палки
    s=xcenter-(int)diff.Div(10L).toLong()-15+ml.distance/10;
    if(s>0 && s<getWidth())
    {
      g.drawLine(s,yLand,s,yLand-10);
      //
      g.drawLine(s-2,yLand-10,s,yLand-7);
      g.drawLine(s+2,yLand-10,s,yLand-7);
    }
    if(s+30>0 && s+30<getWidth())
    {
      g.drawLine(s+30,yLand,s+30,yLand-10);
      //
      g.drawLine(s+28,yLand-10,s+30,yLand-7);
      g.drawLine(s+32,yLand-10,s+30,yLand-7);
    }
    // Случайный пизаж
    int k=(int)world.x.Div(10L).toLong()-getWidth()/2;
    while(k<0)
      k+=getWidth();
    for(int i=1; i<getWidth(); i++)
    {
      g.drawLine(i-1, yLand-land[((k+i-1)%100)], i, yLand-land[((k+i)%100)]);
    }
    // Стоим на земле, готовы к взлету
    if(world.y.Equal(Float.ZERO) && ml.fuel.Great(Float.ZERO) && !ml.lostControl && Math.abs(ml.distance-ml.lastDistance.toLong())>100)
    {
      if(takeoff_visible)
        g.drawString("Press <2> for takeoff",getWidth()/2,getHeight()/4,g.TOP|g.HCENTER);
      takeoff_visible=!takeoff_visible;
    }
    // Пока не отлетели на 100m вбок мигает 'This way ->'
    if(world.y.Great(Float.ZERO) && world.x.Less(100L))
    {
      if(thisway_visible)
        g.drawString("This way ->",getWidth()/2,getHeight()/4,g.BOTTOM|g.HCENTER);
      thisway_visible=!thisway_visible;
    }
    // Приземлились или потеряли управление
    if(ml.landing || ml.lostControl)
    {
      g.setColor( 255, 255, 255 );
      int n=font.stringWidth("Close");
      g.fillRect(getWidth()-n,getHeight()-y,getWidth(),getHeight());
      g.setColor( 0, 0, 0 );
      g.drawString("Close",getWidth(),getHeight(),g.RIGHT|g.BOTTOM);
    }
    // Если кислород закончился пишем 'Life is over', далее полет неуправляем
    if(curTime>ml.oxygen*60)
    {
      ml.lostControl=true;
      //
      Font font2=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
      g.setFont(font2);
      g.drawString( "Life is over!", xcenter, ycenter, g.BOTTOM | g.HCENTER );
    }
    // Если кислорода меньше чем на 2 минуты, мигает O2
    if(ml.oxygen*60-curTime<120)
    {
      if(o2==null)
      {
        try
        {
          o2=Image.createImage("/O2.png");
        }
        catch(IOException e) { }
      }
      //
      if(o2!=null && (o2_visible || ml.oxygen*60-curTime<=0))
        dg.drawImage(o2,getWidth()-1,getHeight()/2,g.BOTTOM|g.RIGHT,0);
      o2_visible=!o2_visible;
    }
    // Если топлива меньше 100kg мигает F
    if(ml.fuel.toLong()<100)
    {
      if(fimg==null)
      {
        try
        {
          fimg=Image.createImage("/F.png");
        }
        catch(IOException e) { }
      }
      //
      if(fimg!=null && (fimg_visible || ml.fuel.toLong()<=0))
        dg.drawImage(fimg,getWidth()-1,getHeight()/2+3,g.TOP|g.RIGHT,0);
      fimg_visible=!fimg_visible;
    }
    //
    g2.drawImage(bufferImage, 0, 0, g.LEFT|g.TOP);
  }
}
