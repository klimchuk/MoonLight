package henson.games;

import javax.microedition.lcdui.*;
import com.nokia.mid.ui.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class FirstCanvas extends FullCanvas
{
  private MoonLight ml=null;
  public int Page=0;
  public FirstCanvas(MoonLight ml)
  {
    this.ml=ml;
  }
  protected void paint(Graphics g)
  {
    DirectGraphics dg = DirectUtils.getDirectGraphics(g);
    int cx=getWidth();
    int cy=getHeight();
    g.setColor( 255, 255, 255 );
    g.fillRect( 0, 0, cx, cy );
    g.setColor( 0, 0, 0 );
    /**@todo Implement this javax.microedition.lcdui.Canvas abstract method*/
    Font font=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    Font font2=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int y=font.getHeight()+1;
    g.setFont(font);
    g.drawString("MoonLight",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
    switch(Page)
    {
      case 0:
          //
        g.setFont(font2);
        g.drawString("Your mission is flights",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("for transportion of",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("people and cargoes.",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("You get money for it.",0,4*y,Graphics.TOP|Graphics.LEFT);
        //g.drawString("Next>",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        dg.fillTriangle(cx/2-10, cy-7, cx/2+10, cy-7, cx/2, cy-1, 0xff000000);
        break;
      case 1:
          //
        g.setFont(font2);
        g.drawString("On this money you can",0,y,Graphics.TOP|Graphics.LEFT);
        g.drawString("buy fuel and oxygen",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("to continue activity.",0,3*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("It's not so simple!",0,4*y,Graphics.TOP|Graphics.LEFT);
        //g.drawString("Next>",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        dg.fillTriangle(cx/2-10, cy-7, cx/2+10, cy-7, cx/2, cy-1, 0xff000000);
        break;
      case 2:
        g.setFont(font2);
        g.drawString("Control keys",getWidth()/2,y,Graphics.TOP|Graphics.HCENTER);
        g.drawString("Power: 2-More, 8-Less",0,2*y,Graphics.TOP|Graphics.LEFT);
        g.drawString("Angle: 4-Less, 6-More",0,3*y,Graphics.TOP|Graphics.LEFT);
        //g.drawString("Next>",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        dg.fillTriangle(cx/2-10, cy-7, cx/2+10, cy-7, cx/2, cy-1, 0xff000000);
        break;
      case 3:
        g.drawString("Take care &",getWidth()/2,2*y,Graphics.TOP|Graphics.HCENTER);
        g.drawString(" Good luck",getWidth()/2,3*y,Graphics.TOP|Graphics.HCENTER);
        g.setFont(font2);
        g.drawString("[henson.newmail.ru]",0,4*y,Graphics.TOP|Graphics.LEFT);
        //g.drawString("Next>",getWidth(),getHeight(),Graphics.BOTTOM|Graphics.RIGHT);
        dg.fillTriangle(cx/2-10, cy-7, cx/2+10, cy-7, cx/2, cy-1, 0xff000000);
        break;
    }
  }

  protected void keyPressed( int keyCode )
  {
      if(keyCode==KEY_DOWN_ARROW)
      {
        if(Page<3)
        {
          Page++;
          repaint();
        }
        else
        {
          ml.setDisplay(0);
          Page=0;
        }
      }
  }
}