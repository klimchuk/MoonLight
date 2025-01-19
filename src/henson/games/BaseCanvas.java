package henson.games;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import com.nokia.mid.ui.*;
import com.nokia.mid.sound.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BaseCanvas extends FullCanvas
{
  private MoonLight ml=null;
  // 0-menu, 1-contracts, 2-resources
  public int page;
  //
  String[] name=new String[3];
  int[] distance=new int[3];
  int[] weight=new int[3];
  int[] income=new int[3];
  private Random rand=new Random();
  private Image splash=null;
  //
  public BaseCanvas(MoonLight ml, int page)
  {
    this.ml=ml;
    this.page=page;
  }
  protected void paint(Graphics g)
  {
    g.setColor( 255, 255, 255 );
    g.fillRect( 0, 0, getWidth(), getHeight() );
    g.setColor( 0, 0, 0 );
    Font font=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    int y=font.getHeight()+1;
    g.setFont(font);
    Font font2=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    //
    switch(page)
    {
      case -1:
        if(splash==null)
        {
          try
           {
             splash=Image.createImage("/MoonLight.png");
           }
           catch(IOException e) { }
        }
        if(splash!=null)
        {
          DirectGraphics dg = DirectUtils.getDirectGraphics(g);
          dg.drawImage(splash,getWidth()/2,0,g.TOP|g.HCENTER,0);
        }
        else
        {
          g.drawString("MoonLight",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
          g.setFont(font2);
          g.drawString("Press any key...",0,y+4,Graphics.TOP|Graphics.LEFT);
        }
        //
        /*
        try
        {
          ml.s.play(1);
        }
        catch(IllegalArgumentException e) { System.out.print(e); }
        */
        //
        break;
      case 0:
        g.drawString("MoonLight",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.drawString(ml.credits+"$",getWidth(),2*y+4,Graphics.TOP|Graphics.RIGHT);
        g.setFont(font2);
        g.drawString("1. New mission",0,y+4,Graphics.TOP|Graphics.LEFT);
        g.drawString("2. Status",0,2*y+4,Graphics.TOP|Graphics.LEFT);
        g.drawString("3. Info",0,3*y+4,Graphics.TOP|Graphics.LEFT);
        // DEMO BLOCK
        g.drawString("Options",0,getHeight(),Graphics.BOTTOM|Graphics.LEFT);
        g.drawString("Exit",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 1:
        g.drawString("Contracts",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("Credits: "+ml.credits+"$",0,y,Graphics.TOP|Graphics.LEFT);
        for(int i=0; i<3; i++)
        {
          int k=i+1;
          g.drawString(k+". "+distance[i]+"km "+weight[i]+"kg "+income[i]+"$",0,y*(i+2),Graphics.TOP|Graphics.LEFT);
        }
        g.drawString("Back",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 2:
        g.drawString("Resources",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("Credits: "+ml.credits+"$",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Buy fuel and oxygen",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("1. Fuel - "+ml.fuel.toLong()+"kg",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("2. Oxygen - "+ml.oxygen+"min",0,4*y,Graphics.TOP|Graphics.LEFT);
        if(ml.fuel.toLong()>0 && ml.oxygen>0)
          g.drawString("Fly!",0,getHeight(),Graphics.BOTTOM|Graphics.LEFT);
        g.drawString("Back",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 3:
        g.drawString("Status",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("Fuel - "+ml.fuel.toLong()+"kg",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Oxygen - "+ml.oxygen+"min",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Flights - "+ml.flights,0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Flytime - "+ml.flytime/60+"min",0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Back",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 4:
        g.drawString("Summary",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("Credits: "+ml.credits+"$",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Income: "+ml.income+"$",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Repair: -"+ml.repair+"$",0,3*y,Graphics.TOP|Graphics.LEFT);
        int sum=ml.credits+ml.income-ml.repair;
        g.drawString("Sum: "+sum+"$",0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Menu",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        ml.credits=sum;
        break;
      case 5:
        g.drawString("Crashed!",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("I'm so sorry,",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("you are dead now.",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Try one more time!",0,3*y,Graphics.TOP|Graphics.LEFT);
        String mess="?: ";
        if(ml.lostControl)
          mess+="oxygen over";
        else
        if(ml.crashed)
          mess+="hard landing";
        g.drawString(mess,0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Menu",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        ml.InitSituation();
        break;
      case 6:
        g.drawString("Landing",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        long d=Math.abs(ml.distance-ml.lastDistance.toLong());
        g.drawString("Vertical speed: "+ml.lastVy.toLong()+"m/s",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Horizontal speed: "+ml.lastVx.toLong()+"m/s",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Distance to destination",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString(d+"m",0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Summary",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 7:
        g.drawString("Bankrupt!",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("I'm so sorry,",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("you have no money",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("to continue activity.",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Menu",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 8:
        g.drawString("Options",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("1. Load",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("2. Save",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("3. Top list",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("4. Device info",0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Back",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
      case 9:
        g.drawString("Device info",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        // Display
        String strDisplay="Display: ";
        strDisplay=strDisplay.concat(getWidth()+"x"+getHeight()+" "+ml.display.numColors()+" colors");
        // Memory
        String strMem="Memory: ";
        Runtime rt=Runtime.getRuntime();
        rt.gc();
        strMem=strMem.concat(rt.freeMemory()+"/"+rt.totalMemory());
        // Vibro
        String strOther="Other:";
        try
        {
          DeviceControl.startVibra(50, 0);
          strOther=strOther.concat(" Vibro");
        }
        catch(java.lang.IllegalStateException e) { System.out.println(e); }
        catch(java.lang.IllegalArgumentException e) { System.out.println(e); }
        // Sound
        String strSound="Sound:";
        int[] sf=Sound.getSupportedFormats();
        for(int i=0; i<sf.length; i++)
        {
          switch(sf[i])
        {
          case Sound.FORMAT_TONE:
            strSound=strSound.concat(" Tones");
            break;
          case Sound.FORMAT_WAV:
            strSound=strSound.concat(" Waves");
            break;
        }
        }
        //
        g.drawString(strDisplay,0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString(strSound,0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString(strMem,0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString(strOther,0,4*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Back",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        break;
    }
  }
  protected void keyPressed( int keyCode )
  {
    if(page==-1)
    {
      if(splash!=null)
        splash=null;
      page=0;
      repaint();
      return;
    }
    //
    if(keyCode==KEY_NUM1)
    {
      switch(page)
      {
      case 0:
        page=1;
        MakeMarket();
        repaint();
        break;
      case 1:
        page=2;
        ml.income=income[0];
        ml.distance=distance[0]*1000;
        ml.payload=weight[0];
        repaint();
        break;
      case 2:
        ml.setDisplay(4);
        break;
      case 8:
        ml.setDisplay(7);
        break;
      }
    }
    if(keyCode==KEY_NUM2)
    {
      if(page==0)
      {
        page=3;
        repaint();
      }
      else
      if(page==1)
      {
        page=2;
        ml.income=income[1];
        ml.distance=distance[1]*1000;
        ml.payload=weight[1];
        repaint();
      }
      else
      if(page==2)
        ml.setDisplay(5);
      else
      if(page==8)
        ml.setDisplay(8);
    }
    if(keyCode==KEY_NUM3)
    {
      if(page==0)
        ml.setDisplay(1);
      else
      if(page==1)
      {
        page=2;
        ml.income=income[2];
        ml.distance=distance[2]*1000;
        ml.payload=weight[2];
        repaint();
      }
      else
      if(page==8)
        ml.setDisplay(9);
    }
    if(keyCode==KEY_SOFTKEY1)
    {
      // DEMO BLOCK
      if(page==0)
      {
        page=8;
        repaint();
      }
      else
      if(page==2)
        ml.setDisplay(2);
    }
    if(keyCode==KEY_NUM4)
    {
      if(page==8)
      {
        page=9;
        repaint();
      }
    }
    if(keyCode==KEY_SOFTKEY2)
    {
      switch(page)
      {
        case 0:
          ml.quitApp();
          break;
        case 2:
          page=1;
          repaint();
          break;
        case 6:
          ml.setDisplay(3);
          break;
        default:
          if(page==4 && ml.credits<0)
          {
            ml.InitSituation();
            page=7;
          }
          else
            page=0;
          repaint();
      }
    }
  }
  private void MakeMarket()
  {
    for(int i=0; i<3; i++)
    {
      int n=rand.nextInt();
      if(n<0)
        name[i]="Food";
      else
        name[i]="Passengers";
      while((n=rand.nextInt())<0 || n%100<10);
      distance[i]=n%100;
      while((n=rand.nextInt())<0 || n%3000<100);
      weight[i]=n%3000;
      n=rand.nextInt();
      if(n<0)
        income[i]=distance[i]*weight[i]*9/100;
      else
        income[i]=distance[i]*weight[i]*11/100;
    }
  }
}
