package zamani.com.taximeter.helper;

import android.app.Activity;
import android.graphics.Typeface;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import zamani.com.taximeter.R;
import zamani.com.taximeter.model.Place;

public final class GeoFinder {

    private final static String URI = "http://nominatim.openstreetmap.org/search/${query}?format=json";
    private final double departInitPrice;
    private final double pricePerKm;
    private Activity activity;
    private List<EditText> targets;
    private final int SCROLL_VIEW_TAG = 18111988;
    private TextView recipeTextView;
    private Typeface font;
    private ViewGroup parent;
    private final String GEO_FINDER_TAG = "GeoFinder";
    private final String currency;


    public GeoFinder(GeoFinderBuilder builder) {
        this.activity = builder.activity;
        this.targets = builder.targets;
        this.font = builder.font;
        this.recipeTextView = builder.recipeTextView;
        this.departInitPrice = builder.departInitPrice;
        this.pricePerKm = builder.pricePerKm;
        this.currency = builder.currency;
    }

    public void go() {
        for (int i = 0; i <= targets.size() - 1; i++) {
            final EditText targetEditText = targets.get(i);
            targetEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.toString().length() >= 3) {
                        search(targetEditText.getText().toString(), targetEditText);
                        parent = (ViewGroup) targetEditText.getParent();
                    } else {
                        resetScrollLayout();
                    }
                }
            });
        }
    }

    private void search(final String query, final EditText targetEditText) {
        final OkHttpClient client = new OkHttpClient();
        final String url = URI.replace("${query}", query);
        final Request request = new Request.Builder().get()
                .addHeader("User-Agent", "zmi.amn@gmail.com")
                .addHeader("accept-language", "fr")
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                final String result = response.body().string();
                final List<Place> data = parseData(result);
                GeoFinder.this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (data != null && !data.isEmpty()) {
                            // GeoFinder.this.dialog.show();
                            if (targetEditText != null) {
                                resetScrollLayout();
                                buildScrollView(data, targetEditText);
                            }
                        }
                    }
                });
            }
        });
    }

    private void buildScrollView(List<Place> data, final EditText targetEditText) {
        final HorizontalScrollView scrollView = new HorizontalScrollView(activity);
        scrollView.setTag(SCROLL_VIEW_TAG);
        scrollView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 100));
        scrollView.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
        final LinearLayout scrollLayout = new LinearLayout(activity);
        scrollLayout.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
        scrollLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        scrollLayout.setOrientation(LinearLayout.HORIZONTAL);
        scrollView.addView(scrollLayout);
        final RelativeLayout.LayoutParams customLayoutParameters = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        customLayoutParameters.addRule(RelativeLayout.BELOW, targetEditText.getId());
        for (Place place : data) {
            targetEditText.setTag(place);
            buildTextView(place, scrollLayout, targetEditText);
            if (targetEditText.getText().toString().equals(extractShortText(place.getDisplayName()))) {
                return;
            }
        }
        parent.addView(scrollView, customLayoutParameters);
    }

    private void buildTextView(Place place, final LinearLayout scrollLayout, final EditText targetEditText) {
        final TextView textView = new TextView(activity);
        textView.setTag(place);
        textView.setTextSize(15);
        textView.setTypeface(this.font);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(15, 15, 15, 15);
        final LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(targetEditText.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT);
        textViewLayoutParams.leftMargin = 10;
        textViewLayoutParams.rightMargin = 10;
        textViewLayoutParams.bottomMargin = 10;
        textViewLayoutParams.gravity = Gravity.CENTER;
        textView.setTextColor(activity.getResources().getColor(R.color.taxi_color1));
        textView.setLayoutParams(textViewLayoutParams);
        String shortText = extractShortText(place.getDisplayName());
        textView.setText(shortText);

        final LinearLayout roundRectViewParent = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.place_item, scrollLayout, false);
        //roundRectViewParent.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
        roundRectViewParent.addView(textView);

        scrollLayout.addView(roundRectViewParent, textViewLayoutParams);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Place place = (Place) targetEditText.getTag();
                targetEditText.setText(extractShortText(place.getDisplayName()));
                resetScrollLayout();
                Utils.hideKeyboardFrom(activity, targetEditText);
                updateRecipeTextView();
            }
        });
    }

    private String extractShortText(String displayName) {
        if (displayName != null && displayName.split(",").length >= 3) {
            return displayName.split(",")[0] + displayName.split(",")[1] + displayName.split(",")[2];
        } else if (displayName != null && displayName.length() >= 20) {
            return displayName.substring(0, 19);
        } else {
            return displayName;
        }
    }

    private void resetScrollLayout() {
        if (parent != null) {
            HorizontalScrollView oldScrollView = parent.findViewWithTag(SCROLL_VIEW_TAG);
            if (oldScrollView != null) {
                parent.removeView(oldScrollView);
            }
        }
    }

    private List<Place> parseData(String string) {
        final List<Place> places = new ArrayList<>();
        final Gson gson = new Gson();
        try {
            final List<Map<String, Object>> suggestions = gson.fromJson(string, List.class);
            for (Map<String, Object> map : suggestions) {
                final Place place = new Place((String) map.get("display_name"), Double.parseDouble((String) map.get("lat")), Double.parseDouble((String) map.get("lon")));
                places.add(place);
            }
        } catch (Exception e) {
            Log.e(GEO_FINDER_TAG, e.getMessage());
        }
        return places;
    }

    public double distance() {
        double distance = 0;
        if (targets.size() == 2) {
            if (targets.get(0).getTag() != null && targets.get(1).getTag() != null) {
                Place depart = (Place) targets.get(0).getTag();
                Place destination = (Place) targets.get(1).getTag();
                distance = getDistance(new LatLng(depart.getLat(), depart.getLon()), new LatLng(destination.getLat(), destination.getLon()));
            }
        }
        return distance;
    }

    private String calculate(LatLng latlngA, LatLng latlngB) {
        float distance = getDistance(latlngA, latlngB);
        double total = GeoFinder.this.departInitPrice + (distance * GeoFinder.this.pricePerKm);
        return String.format("%.2f", total) + " " + GeoFinder.this.currency;
    }

    private float getDistance(LatLng latLnDeparture, LatLng LatLnDestination) {
        Location locationA = new Location("point A");
        locationA.setLatitude(latLnDeparture.latitude);
        locationA.setLongitude(latLnDeparture.longitude);
        Location locationB = new Location("point B");
        locationB.setLatitude(LatLnDestination.latitude);
        locationB.setLongitude(LatLnDestination.longitude);
        return locationA.distanceTo(locationB) / 1000;
    }

    private void updateRecipeTextView() {
        String total = "";
        if (targets.size() == 2) {
            if (targets.get(0).getTag() != null && targets.get(1).getTag() != null) {
                Place depart = (Place) targets.get(0).getTag();
                Place destination = (Place) targets.get(1).getTag();
                total = calculate(new LatLng(depart.getLat(), depart.getLon()), new LatLng(destination.getLat(), destination.getLon()));
            }
        }
        recipeTextView.setText(total);
    }

    public GeoFinder font(Typeface typefaceLcd) {
        this.font = typefaceLcd;
        return this;
    }

    public static class GeoFinderBuilder {
        private Activity activity;
        private List<EditText> targets = new ArrayList<>();
        private Typeface font;
        private TextView recipeTextView;
        private double departInitPrice;
        private double pricePerKm;
        private String currency;

        public GeoFinderBuilder with(Activity activity) {
            this.activity = activity;
            return this;
        }

        public GeoFinderBuilder withTarget(EditText editText) {
            this.targets.add(editText);
            return this;
        }

        public GeoFinderBuilder withFont(Typeface font) {
            this.font = font;
            return this;
        }

        public GeoFinderBuilder withRecipeTextView(TextView recipeTextView) {
            this.recipeTextView = recipeTextView;
            return this;
        }


        public GeoFinderBuilder withDepartInitPrice(double departInitPrice) {
            this.departInitPrice = departInitPrice;
            return this;
        }

        public GeoFinderBuilder withPricePerKm(double pricePerKm) {
            this.pricePerKm = pricePerKm;
            return this;
        }

        public GeoFinderBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public GeoFinder build() {
            return new GeoFinder(this);
        }
    }
}
