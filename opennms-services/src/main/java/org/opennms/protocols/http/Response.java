package org.opennms.protocols.http;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.opennms.core.http.NVPair;
import org.opennms.core.http.Util;

public class Response
{
  String httpClientVersion = "HttpClient 4.0";
  int statusCode = 0;
  String reasonLine = "";
  String httpVersion = "";

  long contentLength = 0L;
  byte[] data = null;
  String originalURL = "";
  String effectiveURL = "";
  NVPair[] headers = null;
  InputStream is = null;

  String sessionHeaders = "";
  String responseHeaders = "";
  String requestHeaders = "";

  long dnsTime = 0L;
  long connectionTime = 0L;
  long responseTime = 0L;
  long downloadTime = 0L;
  long firstByteTime = 0L;
  long lastByteTime = 0L;
  long sslHandshakeTime = -1L;
  List<Cookie> cookies = new ArrayList();

  int numberOfRedirection = 0;

  String ipaddress = "";

  String failedIP = "";

  public void setHttpClientVersion(String version)
  {
    this.httpClientVersion = version;
  }

  public String getHttpClientVersion()
  {
    return this.httpClientVersion;
  }

  public void setStatusCode(int code)
  {
    this.statusCode = code;
  }

  public int getStatusCode()
  {
    return this.statusCode;
  }

  public void setHttpMessage(String message)
  {
    this.reasonLine = message;
  }

  public String getHttpMessage()
  {
    return this.reasonLine;
  }

  public void setHttpVersion(String version)
  {
    this.httpVersion = version;
  }

  public String getHttpVersion()
  {
    return this.httpVersion;
  }

  public void setContentLength(long len)
  {
    this.contentLength = len;
  }

  public long getContentLength()
  {
    return this.contentLength;
  }

  public void setData(byte[] content)
  {
    this.data = content;
  }

  public void setCookies(List<Cookie> cookies)
  {
    if (cookies == null)
    {
      cookies = new ArrayList();
    }
    this.cookies = cookies;
  }

  public List<Cookie> getCookies() {
    return this.cookies;
  }

  public byte[] getData()
  {
    return this.data;
  }

  public void setOriginalURL(String url)
  {
    this.originalURL = url;
  }

  public String getOriginalURL()
  {
    return this.originalURL;
  }

  public void setEffectiveURL(String url)
  {
    this.effectiveURL = url;
  }

  public URI getOriginalURI()
  {
    try
    {
      URL url = new URL(this.originalURL);
      return url.toURI();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public URI getEffectiveURI()
  {
    try
    {
      String eURL = this.effectiveURL;
      if (eURL.equals(""))
      {
        eURL = this.originalURL;
      }

      URL url = new URL(eURL);
      return url.toURI();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public String getEffectiveURL()
  {
    return this.effectiveURL;
  }

  public void setInputStream(InputStream stream)
  {
    this.is = stream;
  }

  public InputStream getInputStream()
  {
    return this.is;
  }

  public void setResponseHeaders(Header[] h)
  {
    this.headers = new NVPair[h.length];
    for (int i = 0; i < h.length; i++)
    {
      this.headers[i] = new NVPair(h[i].getName(), h[i].getValue());
    }
  }

  public void setSessionHeaders(String headers)
  {
    this.sessionHeaders = headers;
  }

  public String getSessionHeaders()
  {
    return this.sessionHeaders;
  }
  public void setRequestHeaders(String headers) {
    this.requestHeaders = headers;
  }
  public String getRequestHeaders() {
    return this.requestHeaders;
  }
  public void setResponseHeaders(String headers) {
    this.responseHeaders = headers;
  }
  public String getResponseHeaders() {
    return this.responseHeaders;
  }

  public String getHeader(String name) {
    if (this.headers == null)
    {
      return null;
    }
    for (int i = 0; i < this.headers.length; i++)
    {
      if (this.headers[i].getName().equalsIgnoreCase(name))
      {
        return this.headers[i].getValue();
      }
    }
    return null;
  }

  public Enumeration listHeaders()
  {
    if (this.headers == null)
    {
      return null;
    }
    Hashtable respHeaders = new Hashtable();
    for (int i = 0; i < this.headers.length; i++)
    {
      respHeaders.put(this.headers[i].getName(), this.headers[i].getValue());
    }

    return respHeaders.keys();
  }

  public int getHeaderAsInt(String name) throws NumberFormatException
  {
    if (this.headers == null)
    {
      return -1;
    }

    for (int i = 0; i < this.headers.length; i++)
    {
      if (!this.headers[i].getName().equalsIgnoreCase(name))
        continue;
      try
      {
        if (this.headers[i].getValue().equalsIgnoreCase(""))
        {
          return 0;
        }

        return Integer.parseInt(this.headers[i].getValue());
      }
      catch (NumberFormatException nfe)
      {
        return -1;
      }
    }

    return -1;
  }

  public void setNumberOfRedirects(int redirects)
  {
    this.numberOfRedirection = redirects;
  }

  public int getNumberOfRedirects()
  {
    return this.numberOfRedirection;
  }

  public void setResponseTime(long time)
  {
    this.responseTime = time;
  }

  public long getResponseTime()
  {
    return this.responseTime;
  }

  public void setDNSTime(long time)
  {
    this.dnsTime = time;
  }

  public long getDNSTime()
  {
    return this.dnsTime;
  }

  public void setSSLHandshakeTime(long time)
  {
    this.sslHandshakeTime = time;
  }

  public long getSSLHandshakeTime()
  {
    return this.sslHandshakeTime;
  }

  public void setConnectionTime(long time)
  {
    this.connectionTime = time;
  }

  public long getConnectionTime()
  {
    return this.connectionTime;
  }

  public void setDownloadTime(long time)
  {
    this.downloadTime = time;
  }

  public long getDownloadTime()
  {
    return this.downloadTime;
  }

  public void setFirstByteTime(long time)
  {
    this.firstByteTime = time;
  }

  public long getFirstByteTime()
  {
    return this.firstByteTime;
  }

  public void setLastByteTime(long time)
  {
    this.lastByteTime = time;
  }

  public long getLastByteTime()
  {
    return this.lastByteTime;
  }

  public void setIP(String ip)
  {
    this.ipaddress = ip;
  }

  public String getIP()
  {
    return this.ipaddress;
  }

  public void setFailedIP(String ips)
  {
    this.failedIP = ips;
  }

  public String getFailedIP()
  {
    return this.failedIP;
  }

  public String getResponseWithOutHeaders()
  {
    StringBuilder sb = new StringBuilder("");
    try
    {
      sb.append("[OrigURL=").append(this.originalURL).append(", EffectiveURL=").append(this.effectiveURL).append(", IP=").append(this.ipaddress).append(", RedirectCount=").append(this.numberOfRedirection).append(", Status Code:").append(this.statusCode).append(", Content Length:").append(this.contentLength).append("]");
      sb.append("\n Response Times: [DNS=").append(this.dnsTime).append(", CONN=").append(this.connectionTime).append(", FBT=").append(this.firstByteTime).append(", LBT=").append(this.lastByteTime).append(", DT=").append(this.downloadTime).append(", RT=").append(this.responseTime).append(", SSLHST=").append(this.sslHandshakeTime).append("]");
    }
    catch (Exception e)
    {
      sb.append("Exception in toString : ").append(Util.trim(e.getMessage()));
    }
    return sb.toString();
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder("");
    try
    {
      sb.append("[OrigURL=").append(this.originalURL).append(", EffectiveURL=").append(this.effectiveURL).append(", IP=").append(this.ipaddress).append(", RedirectCount=").append(this.numberOfRedirection).append(", Status Code:").append(this.statusCode).append(", Content Length:").append(this.contentLength).append("]");
      sb.append("\n Session Headers: ").append(this.sessionHeaders);
      sb.append("\n Response Times: [DNS=").append(this.dnsTime).append(", CONN=").append(this.connectionTime).append(", FBT=").append(this.firstByteTime).append(", LBT=").append(this.lastByteTime).append(", DT=").append(this.downloadTime).append(", RT=").append(this.responseTime).append(", SSLHST=").append(this.sslHandshakeTime).append("]");
    }
    catch (Exception e)
    {
      sb.append("Exception in toString : ").append(Util.trim(e.getMessage()));
    }
    return sb.toString();
  }
}