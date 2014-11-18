package us.ba3.altusdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import us.ba3.me.MapView;
import us.ba3.me.virtualmaps.SphericalMercatorRasterTile;
import us.ba3.me.virtualmaps.TileProvider;
import us.ba3.me.virtualmaps.TileProviderRequest;
import us.ba3.me.virtualmaps.TileProviderResponse;
import us.ba3.me.virtualmaps.VirtualMapInfo;

/**
 * Demonstrates passing a Bitmap as an image tile. Based on TileProviderLargeImageSamplingTest
 *
 * @author Luke Fielke
 */
public class BitmapTileTest extends METest implements TileProvider {

    private static final String TAG = "BitmapTileTest";


    public BitmapTileTest(String name) {
        this.name = name;
    }


    @Override
    public void start() {
        //Add virtual map
        VirtualMapInfo mapInfo = new VirtualMapInfo();
        mapInfo.name = name;
        mapInfo.zOrder = 200;
        mapInfo.setTileProvider(this);
        mapView.addMapUsingMapInfo(mapInfo);
    }

    @Override
    public boolean requiresDownloadableAssets() {
        return false;
    }

    @Override
    public void stop() {
        mapView.removeMap(this.name, false);
    }

    @Override
    public void requestTile(TileProviderRequest tileRequest) {
        Log.i(TAG, "requestTile called, creating TileMaker");
        TileMaker t = new TileMaker(tileRequest, this.mapView);
        new Thread(t).start();
    }

    private static class TileMaker implements Runnable {

        private TileProviderRequest tileRequest = null;
        private MapView mapView = null;

        private TileMaker(final TileProviderRequest tileRequest, final MapView mapView) {
            this.mapView = mapView;
            this.tileRequest = tileRequest;
        }

        void errorTile(TileProviderRequest tileRequest) {
            tileRequest.cachedImageName = "errorTexture";
            tileRequest.responseType = TileProviderResponse.kTileResponseRenderNamedCachedImage;
            this.mapView.tileLoadComplete(tileRequest);
        }

        @Override
        public void run() {
            Log.d(TAG, "run() called");

            for (SphericalMercatorRasterTile t : tileRequest.sphericalMercatorRasterTiles) {
                InputStream inputStream;
                try {
                    inputStream = mapView.getContext().getAssets().open("VectorMapTextures/apple_golf@2x.png");
                } catch (IOException e) {
                    Log.e(TAG, "IOException opening asset file: " + e);
                    return;
                }
                Log.i(TAG, "Opened InputStream");

                // Load Bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;   //explicit setting!


                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                if (bitmap == null) {
                    Log.w(TAG, "decodeStream failed");
                    errorTile(tileRequest);
                    return;
                }
                Log.i(TAG, "Got bitmap " + options.outWidth + "x" + options.outHeight + "px");
                t.setImage(bitmap, false);
            }

            //We're done
            tileRequest.responseType = TileProviderResponse.kTileResponseRenderImage;
            this.mapView.tileLoadComplete(tileRequest);
        }
    }
}

