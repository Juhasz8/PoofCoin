package com.example.poof_ui;
import java.util.*;

public class CurrentEventManager {

    // Category 1 events. 5% Probability.
    List<CurrentEvent> category1 = Arrays.asList(
            new CurrentEvent("A major government adopts the cryptocurrency as its official currency.", 5, 50, 50, 50),
            new CurrentEvent("A significant vulnerability is discovered in the cryptocurrency's underlying technology.", 5, 80, 50, 50),
            new CurrentEvent("The cryptocurrency is recognized as a global reserve currency by a major financial institution.", 5, 100, 50, 50)
    );

    // Category 2 events. 15% Probability.
    List<CurrentEvent> category2 = Arrays.asList(
            new CurrentEvent("A prominent celebrity publicly endorses the cryptocurrency.", 15, 5, 50, 50),
            new CurrentEvent("Regulatory authorities introduce favorable regulations for the cryptocurrency.", 15, 10, 50, 50),
            new CurrentEvent("A major merchant starts accepting the cryptocurrency as a form of payment.", 15, 10, 50, 50),
            new CurrentEvent("The cryptocurrency's development team announces a breakthrough technological advancement.", 15, 40, 50, 50)
    );

    // Category 3 events. 30% Probability.
    List<CurrentEvent> category3 = Arrays.asList(
            new CurrentEvent("A significant security breach occurs in a major cryptocurrency exchange.", 30, 40, 50, 50),
            new CurrentEvent("A large-scale hack targets multiple wallets holding the cryptocurrency.", 30, 50, 50, 50),
            new CurrentEvent("The cryptocurrency's community successfully implements a major upgrade.", 30, 20, 50, 50),
            new CurrentEvent("The cryptocurrency experiences a surge in adoption in a specific geographic region.", 30, 5, 50, 50),
            new CurrentEvent("Negative media coverage highlights the involvement of the cryptocurrency in illicit activities.", 30, 5, 50, 50)
    );

    // Category 4 events. 50% Probability.
    List<CurrentEvent> category4 = Arrays.asList(
            new CurrentEvent("A market-wide correction leads to a temporary decline in the cryptocurrency's price.",50, 5, 50, 50),
            new CurrentEvent("A competing cryptocurrency gains substantial popularity and market share.", 50, 10, 50, 50),
            new CurrentEvent("Economic instability in a major country prompts investors to seek alternative assets.", 50, 10, 50, 50),
            new CurrentEvent("The cryptocurrency's development team faces internal conflicts and slows down progress.", 50, 20, 50, 50),
            new CurrentEvent("A prominent regulatory body issues a warning or imposes restrictions on the cryptocurrency.", 50, 20, 50, 50)
    );

    // Get an event
    public CurrentEvent getEvent() {

        Map<Integer, List<Integer>> dictionary = Map.of(
                5, List.of(0, 6), // Key 5 corresponds to the range [0, 6]
                15, List.of(6, 21),  // Key 15 corresponds to the range [6, 21]
                30, List.of(21, 51), // Key 30 corresponds to the range [21, 51]
                50, List.of(51, 100) // Key 50 corresponds to the range [51, 100]
        );
        Random randomCategoryNumber = new Random();
        int myRandomCategoryNumber = randomCategoryNumber.nextInt(101); // Generate a random event number between 0 and 100 (inclusive)
        int myKey = 0;

        // Iterate through the dictionary entries
        for (Map.Entry<Integer, List<Integer>> entry : dictionary.entrySet()) {
            String key = String.valueOf(entry.getKey());
            List<Integer> value = entry.getValue();

            // Check if the random event number falls within the range of the current dictionary entry
            if (myRandomCategoryNumber > value.get(0) && myRandomCategoryNumber < value.get(1)) {
                myKey = Integer.parseInt(key); // Set myKey to the corresponding key value
            }
        }

        // Return the event based on the matched myKey value
        Random randomEvent = new Random();
        if (myKey == 5) {
            int cat1Random = randomEvent.nextInt(0,3);
            return category1.get(cat1Random);
        } else if (myKey == 15) {
            int cat2Random = randomEvent.nextInt(0,4);
            return category2.get(cat2Random);
        } else if (myKey == 30) {
            int cat3Random = randomEvent.nextInt(0,5);
            return category3.get(cat3Random);
        } else if (myKey == 50) {
            int cat4Random = randomEvent.nextInt(0,5);
            return category4.get(cat4Random);
        }

        return null; // Return null if no matching event is found
    }

}
