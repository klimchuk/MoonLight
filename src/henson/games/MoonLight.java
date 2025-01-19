package henson.games;

import java.io.*;
import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import javax.microedition.io.*;
import com.nokia.mid.sound.*;
import henson.midp.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MoonLight extends MIDlet implements CommandListener
{
  private static MoonLight instance;
  /*
  private  final  static  byte[] BULLET_SOUND_BYTES  =
  {
    (byte)0x02, (byte)0x4A, (byte)0x3A, (byte)0x40, (byte)0x04
  };
  public Sound s=new Sound(BULLET_SOUND_BYTES, Sound.FORMAT_TONE);
  */
  private Timer timer = new Timer();
  private World world = null;
  public Display display = null;
  private Command okCommand = new Command("Buy", Command.SCREEN, 1);
  private Command okCommand2 = new Command("Sell", Command.SCREEN, 1);
  private Command okCommandA = new Command("Buy", Command.SCREEN, 1);
  private Command okCommand2A = new Command("Sell", Command.SCREEN, 1);
  private Command okCommand3 = new Command("Load", Command.SCREEN, 1);
  private Command okCommand4 = new Command("Save", Command.SCREEN, 1);
  private Command okCommand5 = new Command("Append", Command.SCREEN, 1);
  private Command cancelCommand = new Command("Cancel", Command.BACK, 1);
  private FirstCanvas displayable1 = new FirstCanvas(this);
  public MainCanvas displayable = new MainCanvas(this);
  private BaseCanvas bc = new BaseCanvas(this, -1);
  private TextBox tb = new TextBox("Title","",3,TextField.NUMERIC);
  public TextBox tbname = new TextBox("Enter name","",30,TextField.ANY);
  public List topList=new List("Top list", List.IMPLICIT);
  private Command appendCommand = new Command("Append", Command.SCREEN, 1);
  private Command backCommand = new Command("Back", Command.BACK, 1);
  public HttpThread obj=null;
  //
  public Float lastVx=new Float();
  public Float lastVy=new Float();
  public Float lastDistance=new Float();
  //
  boolean landing=false;
  boolean crashed=false;
  boolean lostControl=false;
  // money (persistent)
  public int credits=1000;
  // fuel for flight (persistent)
  public Float fuel=new Float();
  // oxygen (min) (persistent)
  public int oxygen=0;
  // number of flights (persistent)
  public int flights=0;
  // flytime (persistent)
  public int flytime=0;
  // income money by contract
  public int income=0;
  // distance between source and destination points
  public int distance=0;
  // repair
  public int repair=0;
  // payload kg
  public int payload=0;
  //
  private RecordStore rs=null;
  /** Constructor */
  public MoonLight()
  {
    instance = this;
  }

  /** Main method */
  public void startApp()
  {
    display=Display.getDisplay(this);
    //
    topList.addCommand(appendCommand);
    topList.addCommand(backCommand);
    //
    setDisplay(-1);
  }

  /** Handle pausing the MIDlet */
  public void pauseApp() {
  }

  /** Handle destroying the MIDlet */
  public void destroyApp(boolean unconditional) {
  }

  /** Quit the MIDlet */
  public static void quitApp() {
    instance.destroyApp(true);
    instance.notifyDestroyed();
    instance = null;
  }

  public void Landing()
  {
    landing=true;
  }

  public boolean isCrashed()
  {
    return (Math.abs(lastVy.toLong())>5 ||
            Math.abs(lastVx.toLong())>5 ||
            Math.abs(world.angle)>5);
  }

  public void setDisplay(int n)
  {
    switch(n)
    {
      case -1:
        bc.page=-1;
        display.setCurrent(bc);
        break;
      case 0:
        bc.page=0;
        display.setCurrent(bc);
        break;
      case 1:
        display.setCurrent(displayable1);
        break;
      case 2:
        world=new World(this);
        displayable.world=world;
        displayable.startTime=System.currentTimeMillis();
        world.Init();
        display.setCurrent(displayable);
        timer.schedule(world, 0, 100);
        break;
      case 3:
        crashed=isCrashed();
        repair=(int)(lastVx.Mul(lastVx).Add(lastVy.Mul(lastVy)).toLong());
        repair*=10;
        //
        long d=Math.abs(distance-lastDistance.toLong())*income/distance;
        repair+=d;
        //
        if(crashed || lostControl)
          bc.page=5;
        else
          bc.page=4;
        display.setCurrent(bc);
        bc.repaint();
        //
        landing=false;
        //
        break;
      case 4:
        int maxFuel=credits;
        if(credits>World.fuelMax-fuel.toLong())
          maxFuel=World.fuelMax-(int)fuel.toLong();
        String strMF=Integer.toString(maxFuel);
        tb.setMaxSize(4);
        tb.setTitle("Fuel, kg");
        tb.addCommand(okCommand);
        tb.addCommand(okCommand2);
        tb.setCommandListener(this);
        display.setCurrent(tb);
        tb.setString(strMF);
        break;
      case 5:
        int maxOxygen=credits/10;
        if(maxOxygen>World.oxygenMax-oxygen)
          maxOxygen=World.oxygenMax-oxygen;
        String strMO=Integer.toString(maxOxygen);
        tb.setMaxSize(3);
        tb.setTitle("Oxygen, min");
        tb.setString(Integer.toString(maxOxygen));
        tb.addCommand(okCommandA);
        tb.addCommand(okCommand2A);
        tb.setCommandListener(this);
        display.setCurrent(tb);
        tb.setString(strMO);
        break;
      case 6:
        if(world!=null)
          world.cancel();
        //
        if(landing)
        {
          bc.page=6;
          display.setCurrent(bc);
        }
        else
          setDisplay(3);
        break;
        // FULL VERSION
      case 7:
        tbname.addCommand(okCommand3);
        tbname.setCommandListener(this);
        display.setCurrent(tbname);
        break;
      case 8:
        tbname.addCommand(okCommand4);
        tbname.setCommandListener(this);
        display.setCurrent(tbname);
        break;
      case 9:
        while(topList.size()>0)
          topList.delete(0);
        //
        obj=new HttpThread(this, "http://hosting.gotdotnet.ru/henson/TopList.aspx", 0);
        obj.addCommand(cancelCommand);
        obj.setCommandListener(this);
        display.setCurrent(obj);
        obj.start();
        //
        break;
      case 10:
        if(obj!=null)
        {
          String str=obj.str;
          StringBuffer b=new StringBuffer();
          for(int i=0; i<str.length(); i++)
          {
            if(obj.str.charAt(i)=='\n')
            {
              topList.append(b.toString(), (Image)null);
              b.setLength(0);
            }
            else
              b.append(str.charAt(i));
          }
          //
          if(b.length()>0)
            topList.append(str.toString(), (Image)null);
          //
          topList.setCommandListener(this);
          display.setCurrent(topList);
        }
        break;
    }
  }
  public void commandAction(Command parm1, Displayable parm2)
  {
    if(parm1==cancelCommand)
    {
      if(parm2==obj)
      {
        obj.stop();
        obj=null;
        setDisplay(0);
      }
    }
    if(parm1==backCommand)
    {
      if(parm2==topList)
      {
        bc.page=0;
        display.setCurrent(bc);
      }
    }
    if(parm1==appendCommand)
    {
      if(parm2==topList)
      {
        tbname.addCommand(okCommand5);
        tbname.setCommandListener(this);
        display.setCurrent(tbname);
      }
    }
    if(parm1==okCommand)
    {
      if(parm2==tb)
      {
        String str=tb.getString();
        int newfuel=0;
        try
        {
          newfuel=Integer.parseInt(str);
        }
        catch(NumberFormatException e) { }
        if(newfuel<=credits && newfuel<=World.fuelMax-fuel.toLong() && newfuel>=0)
        {
          fuel=fuel.Add(new Float(newfuel));
          credits-=newfuel;
          bc.page=2;
          display.setCurrent(bc);
          //
          tb.removeCommand(okCommand);
          tb.removeCommand(okCommand2);
        }
      }
    }
    if(parm1==okCommand2)
    {
      if(parm2==tb)
      {
        String str=tb.getString();
        int newfuel=0;
        try
        {
          newfuel=Integer.parseInt(str);
        }
        catch(NumberFormatException e) { }
        if(newfuel<=fuel.toLong() && newfuel>=0)
        {
          fuel=fuel.Sub(new Float(newfuel));
          credits+=newfuel;
          bc.page=2;
          display.setCurrent(bc);
          //
          tb.removeCommand(okCommand);
          tb.removeCommand(okCommand2);
        }
      }
    }
    if(parm1==okCommandA)
    {
      if(parm2==tb)
      {
        String str=tb.getString();
        int newoxygen=0;
        try
        {
          newoxygen=Integer.parseInt(str);
        }
        catch(NumberFormatException e) { }
        if(newoxygen<=credits/10 && newoxygen<=World.oxygenMax-oxygen && newoxygen>=0)
        {
          oxygen+=newoxygen;
          credits-=newoxygen*10;
          bc.page=2;
          display.setCurrent(bc);
          //
          tb.removeCommand(okCommandA);
          tb.removeCommand(okCommand2A);
        }
      }
    }
    if(parm1==okCommand2A)
    {
      if(parm2==tb)
      {
        String str=tb.getString();
        int newoxygen=0;
        try
        {
          newoxygen=Integer.parseInt(str);
        }
        catch(NumberFormatException e) { }
        if(newoxygen<=oxygen && newoxygen>=0)
        {
          oxygen-=newoxygen;
          credits+=newoxygen*10;
          bc.page=2;
          display.setCurrent(bc);
          //
          tb.removeCommand(okCommandA);
          tb.removeCommand(okCommand2A);
        }
      }
    }
    // Load
    if(parm1==okCommand3)
    {
      if(parm2==tbname)
      {
        ByteArrayInputStream bin = null;
        DataInputStream din = null;
        try
        {
            rs = RecordStore.openRecordStore( tbname.getString(), false );
            byte[] data = rs.getRecord( 1 );
            if( data != null )
            {
              bin = new ByteArrayInputStream(data);
              din = new DataInputStream( bin );
              credits=din.readInt();
              fuel.m_Val=din.readLong();
              fuel.m_E=din.readLong();
              oxygen=din.readInt();
              flights=din.readInt();
              flytime=din.readInt();
              rs.closeRecordStore();
            }
            rs.closeRecordStore();
        }
        catch(RecordStoreException e){
            System.out.println( e );
        }
        catch(IOException e) {
             System.out.println( e );
        }
        //
        bc.page=0;
        display.setCurrent(bc);
        //
        tbname.removeCommand(okCommand3);
      }
    }
    // Save
    if(parm1==okCommand4)
    {
      if(parm2==tbname)
      {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        String str=tbname.getString();
        //
        try
        {
          RecordStore.deleteRecordStore( str );
        }
        catch( RecordStoreException e )
        {
        }
        //
        try
        {
            rs = RecordStore.openRecordStore( str, true );
            //
            dout.writeInt(credits);
            dout.writeLong(fuel.m_Val);
            dout.writeLong(fuel.m_E);
            dout.writeInt(oxygen);
            dout.writeInt(flights);
            dout.writeInt(flytime);
            rs.addRecord(bout.toByteArray(),0,bout.size());
            //
            rs.closeRecordStore();
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
        //
        bc.page=0;
        display.setCurrent(bc);
        //
        tbname.removeCommand(okCommand4);
      }
    }
    // Save
    if(parm1==okCommand5)
    {
      if(parm2==tbname)
      {
        obj=new HttpThread(this, "http://hosting.gotdotnet.ru/henson/MoonLight.aspx", 1);
        obj.addCommand(cancelCommand);
        obj.setCommandListener(this);
        display.setCurrent(obj);
        obj.start();
      }
    }
  }
  void InitSituation()
  {
    lostControl=landing=crashed=false;
    // money
    credits=1000;
    // fuel for flight
    fuel=Float.ZERO;
    // oxygen (min)
    oxygen=0;
    // number of flights
    flights=0;
    // flytime
    flytime=0;
    // income money by contract
    income=0;
    // distance between source and destination points
    distance=0;
    // repair
    repair=0;
    // payload kg
    payload=0;
  }
}
