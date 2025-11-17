import java.util.*;

public class CardValueGenerator {

    private static final Map<String, List<String>> THEMES = Map.of(
            "Black and White Icons", Arrays.asList("⚫","⚪","⬛","⬜","◼","◻","⚙️","💀"),
            "Nature", Arrays.asList("🌲","🌸","🍀","🌻","🌊","🌳","🍁","🌿"),
            "Space Exploration", Arrays.asList("🚀","🪐","🌌","🌕","☄️","👩‍🚀","🛰️","🌟"),
            "Holidays", Arrays.asList("🎄","🎃","🎆","🎁","🕯️","🧧","🥂","🎅"),
            "Art & Paintings", Arrays.asList("🎨","🖌️","🖼️","🪶","🪞","✏️","🖋️","📜")
    );

    public static List<String> generate(String theme, int pairsNeeded) {
        List<String> pool = THEMES.getOrDefault(theme,
                Arrays.asList("🍎","🍌","🍇","🍓"));

        List<String> shuffledPool = new ArrayList<>(pool);
        Collections.shuffle(shuffledPool);

        List<String> result = new ArrayList<>();

        for (int i = 0; i < pairsNeeded; i++) {
            String val = shuffledPool.get(i % shuffledPool.size());
            result.add(val);
            result.add(val);
        }

        Collections.shuffle(result);
        return result;
    }
}
