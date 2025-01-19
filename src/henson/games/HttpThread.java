package henson.games;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HttpThread extends Canvas implements Runnable
{
  private static final int BUFFER_SIZE = 8192;
  private MoonLight ml;
  private String url;
  private int mode;
  private Thread t=null;
  private HttpConnection conn=null;
  public String str;
  //
  public HttpThread(MoonLight ml, String url, int mode)
  {
    this.ml=ml;
    this.url=url;
    this.mode=mode;
  }
  public void start()
  {
    t=new Thread(this);
    t.start();
  }
  public synchronized void stop()
  {
    if(conn!=null)
    {
      try
      {
        conn.close();
      }
      catch(IOException e)
      {
      }
      conn=null;
    }
    if(t!=null)
      t=null;
  }
  public void run()
  {
    switch(mode)
    {
      case 0:
        connect();
        break;
      case 1:
        connect1();
        break;
    }
  }
  protected void paint(Graphics g)
  {
    switch(mode)
    {
      case 0:
        g.drawString("Top list",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        break;
      case 1:
        g.drawString("Append",getWidth()/2,0,Graphics.TOP|Graphics.HCENTER);
        break;
    }
    g.drawString("Connecting...",0,getHeight()/2,Graphics.TOP|Graphics.LEFT);
    if(conn!=null)
      g.drawString("OK",0,getHeight()/2,Graphics.TOP|Graphics.RIGHT);
  }
  public void connect1()
  {
    try
    {
      HttpConnection conn = (HttpConnection)Connector.open(url);
      String str="Name="+ml.tbname.getString()+"&Credits="+Integer.toString(ml.credits)+"\r\n";
      String lenstr=""+str.length();
      conn.setRequestMethod(HttpConnection.POST);
      conn.setRequestProperty("Content-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
      conn.setRequestProperty("Content-Language", "en-EN");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Content-Length", lenstr);
      OutputStream os = conn.openOutputStream ();
      os.write (str.getBytes());
      os.flush();
      os.close ();
      int code=conn.getResponseCode();
      conn.close ();
    }
    catch(IOException e)
    {
    }
    if(ml.obj==this)
      ml.setDisplay(9);
  }
  public void connect()
  {
    DataInputStream dis=null;
    //
    try
    {
      HttpConnection conn = (HttpConnection)Connector.open(url, Connector.READ, true);
      conn.setRequestMethod(HttpConnection.GET);
//    conn.setRequestProperty("If-Modified-Since", "29 Oct 1999 19:43:31 GMT");
      conn.setRequestProperty("Content-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
//    conn.setRequestProperty("Accept", "text/plain");
      conn.setRequestProperty("Content-Language", "en-EN");
      //
      if (conn.getResponseCode() == HttpConnection.HTTP_OK)
      {
        repaint();
        // Retrieve the response back from the servlet
        dis = new DataInputStream(conn.openInputStream());
        // Check the Content-Length first
        byte[] buffer=new byte[BUFFER_SIZE];
        //
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int length = (int)conn.getLength();	// Value from Content-Length header
        if (length != -1) {
                // HTTP 1.1 mode with keepalive
                // Read exactly "length" bytes from the connection
                int count = 0;
                while (count < length) {
                        int bytes = dis.read(buffer, 0, Math.min(BUFFER_SIZE, length - count));
                        if (bytes == -1) {
                                // Unexpected end of file
                                break;
                        }

                        // Append data to the page
                        baos.write(buffer, 0, bytes);

                        count += bytes;
                }
        }
        else
        {
                // Read until the connection is closed
                int count;
                while ((count = dis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        // Append data to the page
                        baos.write(buffer, 0, count);
                }
                // End of file
        }

// Get the whole page
        byte[] data=baos.toByteArray();
        str = new String(data);
        baos.close();	// Discard content
        // Find the begin
        int pos=str.indexOf("<p id=\"start\">");
        if(pos!=-1)
        {
          str=str.substring(pos+14);
          // Find the end
          pos=str.indexOf("</p>");
          str=str.substring(0, pos);
        }
        //
        String key = "";
        String value = "";
        int i=0;
        while ((value = conn.getHeaderField(i)) != null)
        {
             key = conn.getHeaderFieldKey(i++);
             str = str + key + ":" + value + "\n";
        }
        //
        dis.close();
      }
      else
      {
        str="HTTP error:"+conn.getResponseCode();
      }
      conn.close ();
    }
    catch(IOException e)
    {
      str=e.toString();
    }
    //
    if(ml.obj==this)
      ml.setDisplay(10);
  }
}