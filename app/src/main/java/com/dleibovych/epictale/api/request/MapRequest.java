package com.dleibovych.epictale.api.request;

import com.dleibovych.epictale.api.CommonRequest;
import com.dleibovych.epictale.api.CommonResponseCallback;
import com.dleibovych.epictale.api.HttpMethod;
import com.dleibovych.epictale.api.response.MapResponse;
import com.dleibovych.epictale.util.RequestUtils;

import org.json.JSONException;

/**
 * @author Hamster
 * @since 07.10.2014
 * todo undocumented request
 */
public class MapRequest extends CommonRequest
{

  private static final String URL = "https://the-tale.org/dcont/map/region-%s.js";

  private final String mapVersion;

  public MapRequest(final String mapVersion)
  {
    this.mapVersion = mapVersion;
  }

  public void execute(final CommonResponseCallback<MapResponse, String> callback)
  {
    execute(String.format(URL, mapVersion), HttpMethod.GET, null, null, new CommonResponseCallback<String, Throwable>()
    {
      @Override
      public void processResponse(String response)
      {
        try
        {
          RequestUtils.processResultInMainThread(callback, false, new MapResponse(response), null);
        } catch (JSONException e)
        {
          RequestUtils.processResultInMainThread(callback, true, null, e.getLocalizedMessage());
        }
      }

      @Override
      public void processError(Throwable error)
      {
        if (error == null)
        {
          RequestUtils.processResultInMainThread(callback, true, null, null);
          return;
        }
        try
        {
          error.printStackTrace();
        } catch (Exception e)
        {
          e.printStackTrace();
        }
        RequestUtils.processResultInMainThread(callback, true, null, error.getLocalizedMessage());
      }
    });
  }

  @Override
  protected long getStaleTime()
  {
    return 2 * 60 * 60 * 1000; // 2 hours
  }

}
