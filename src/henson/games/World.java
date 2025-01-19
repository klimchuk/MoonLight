package henson.games;

import java.util.TimerTask;
import henson.midp.*;
import com.nokia.mid.ui.*;
/**
 * <p>Title: </p>
 * <p>Description: All about World of the MoonLight</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class World extends TimerTask
{
  // G of the moon
  public static final Float g=new Float(162, -2);
  // Max fuel flow 100 kg/s
  public static final Float maxFlow=new Float(10);
  // 3600
  public static final int qualityFuel=3600;
  // Gross weight of the Moon Shuttle
  public static final int weightGross=3000;
  // Max fuel weight (3000kg)
  public static final int fuelMax=3000;
  // Max oxygen time (60min)
  public static final int oxygenMax=60;
  // Max cargo payload
  public static final int payloadMax=4000;
  //
  public Float x=new Float();
  public Float Vx=new Float();
  public Float y=new Float();
  public Float Vy=new Float();
  //
  public int power = 0;
  public int angle = 0;
  //
  public long lastTime=0;
  // landing time
  public long landingTime=0;
  //
  private MoonLight ml;
  //
  public World(MoonLight ml)
  {
    this.ml=ml;
  }
  public void Init()
  {
    x=new Float();
    y=new Float();
    Vx=new Float();
    Vy=new Float();
    power=0;
    angle=0;
  }
  public boolean Step(long lTime)
  {
    Float t=new Float(lTime);
    t=t.Div(1000L);
    // Active state
    if(power>0)
    {
      Float fuelCur=new Float(ml.fuel);
      Float flowCur=maxFlow.Mul(power).Div(100L);
      Float t1=fuelCur.Div(flowCur);
      // acceleration
      Float a, speed;
      if(t1.Less(t))
      {
        // t1<t
        int curWeight=weightGross+(int)ml.fuel.toLong()/2+ml.payload;
        // acceleration
        // qualityFuel*fuel/curWeight
        a=new Float(qualityFuel);
        a=a.Mul(ml.fuel);
        a=a.Div(curWeight);
        power=0;
        ml.fuel=Float.ZERO;
        //
        speed=new Float(a);
        speed=speed.Mul(t1);
      }
      else
      {
        // t>=t1
        Float fc=flowCur.Mul(t);
        ml.fuel=ml.fuel.Sub(fc);
        int curWeight=weightGross+(int)ml.fuel.toLong()+(int)fc.toLong()/2+ml.payload;
        // acceleration
        // qualityFuel*flow/curWeight
        a=new Float(qualityFuel);
        a=a.Mul(flowCur);
        a=a.Div(curWeight);
        //
        speed=new Float(a);
        speed=speed.Mul(t);
      }
      //
      Float rot=new Float(angle);
      rot=rot.Mul(Float.PI);
      rot=rot.Div(180L);
      Vx=Vx.Add(Float.sin(rot).Mul(speed));
      Vy=Vy.Add(Float.cos(rot).Mul(speed));
    }
    // Passive state
    Vy=Vy.Sub(g.Mul(t));
    //
    x=x.Add(Vx.Mul(t));
    Float yOld=new Float(y);
    y=y.Add(Vy.Mul(t));
    // Касание с землей
    if(y.Less(Float.ZERO))
    {
      if(!ml.landing)
      {
        ml.lastVx=new Float(Vx);
        ml.lastVy=new Float(Vy);
        //System.out.print("Vy="+ml.lastVy.toString()+"\n");
        ml.lastDistance=new Float(x);
      }
      //
      y=Float.ZERO;
      Vy=Float.ZERO;
      Vx=Float.ZERO;
    }
    // Приземлились - вибрация
    if(y.Equal(Float.ZERO) && yOld.Great(Float.ZERO))
    {
      ml.landing=true;
      power=0;
      try
      {
        DeviceControl.startVibra(50, 500);
      }
      catch(java.lang.IllegalStateException e) { System.out.println(e); }
      catch(java.lang.IllegalArgumentException e) { System.out.println(e); }
      //
      if(ml.isCrashed())
      {
        // Очень жесткая посадка!
        ml.setDisplay(6);
      }
    }
    // Если оторвались от земли - вибрация
    if(yOld.Equal(Float.ZERO) && y.Great(Float.ZERO))
    {
      try
      {
        DeviceControl.startVibra(50, 300);
      }
      catch(java.lang.IllegalStateException e) { System.out.println(e); }
      catch(java.lang.IllegalArgumentException e) { System.out.println(e); }
    }
    // Если после посадки снова оторвались от земли, делаем отмену
    if(ml.landing && y.Great(Float.ZERO))
      ml.landing=false;
    //
    return true;
  }
  public void run()
  {
    long curTime=System.currentTimeMillis();
    long diff=curTime-lastTime;
    Step(diff);
    lastTime=curTime;
    ml.displayable.repaint();
  }
  // Rotate array of points around the center point
  public void rotatePoints(int xcenter, int ycenter, int[] x, int[] y, int n)
  {
    Float Angle=new Float(angle);
    Angle=Angle.Mul(Float.PI);
    Angle=Angle.Div(180L);
    Float cosAngle=Float.cos(Angle);
    Float sinAngle=Float.sin(Angle);
    //
    for(int i=0; i<n; i++)
    {
      Float dx=new Float(x[i]-xcenter);
      Float dy=new Float(y[i]-ycenter);
      x[i]=(int)cosAngle.Mul(dx).Sub(sinAngle.Mul(dy)).toLong();
      y[i]=(int)sinAngle.Mul(dx).Add(cosAngle.Mul(dy)).toLong();
      x[i]+=xcenter;
      y[i]+=ycenter;
    }
  }
}