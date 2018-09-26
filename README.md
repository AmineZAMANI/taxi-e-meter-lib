# taxi-e-meter-lib

:taxi: :taxi: :taxi: :taxi: :taxi: :taxi: :taxi: :taxi:
___________________________________________________________________________________
usefull library to estimate the price of a taxi ride.

Usage :
1) Calculate the price between two locations and show the result in a textview : 
```java
 final AppCompatEditText ed1 = findViewById(R.id.edittext1);
 final AppCompatEditText ed2 = findViewById(R.id.edittext2);
 final TextView textViewTotal = findViewById(R.id.result_textview);
 final Typeface typefaceLcd = Typeface.createFromAsset(this.getAssets(), "fonts/lcd.ttf");
 
 final GeoFinder geoFinder = new GeoFinder.GeoFinderBuilder()
                .with(this)
                .withFont(typefaceLcd)
                .withTarget(ed1)
                .withTarget(ed2)
                .withDepartInitPrice(2.4)
                .withPricePerKm(3.5)
                .withCurrency("MAD")
                .withRecipeTextView(textViewTotal)
                .build();
        geoFinder.go();
```
        
 2) Calculate the distance between two locations and returns a double value in Kms :
 ```java
  final Typeface typefaceLcd = Typeface.createFromAsset(this.getAssets(), "fonts/lcd.ttf");
  final AppCompatEditText ed1 = findViewById(R.id.edittext1);
  final AppCompatEditText ed2 = findViewById(R.id.edittext2);
  final GeoFinder geoFinder = new GeoFinder.GeoFinderBuilder()
                .with(this)
                .withFont(typefaceLcd)
                .withTarget(ed1)
                .withTarget(ed2)
                .build();
        geoFinder.go();
        double distance = geoFinder.distance();
```     
        

# Enjoy :blush:
