import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class MovieRecommender {

    static class Movie {
        String title, rating, language, description;
        int year;
        double imdb;
        Set<String> genres;
        Set<String> moods;

        Movie(String[] p) {
            title       = p[0].trim();
            year        = Integer.parseInt(p[1].trim());
            rating      = p[2].trim();
            genres      = parseTags(p[3]);
            moods       = parseTags(p[4]);
            language    = p[5].trim();
            imdb        = Double.parseDouble(p[6].trim());
            description = p[7].trim();
        }

        static Set<String> parseTags(String raw) {
            Set<String> tags = new LinkedHashSet<>();
            for (String part : raw.split(",")) {
                String tag = part.trim().toLowerCase();
                if (!tag.isEmpty()) tags.add(tag);
            }
            return tags;
        }

        boolean matchesGenre(String genre) {
            return genres.contains(genre.trim().toLowerCase());
        }

        boolean matchesMood(String mood) {
            return moods.contains(mood.trim().toLowerCase());
        }

        static String displayTag(String tag) {
            if (tag.equals("sci-fi")) return "Sci-fi";
            if (tag.isEmpty()) return tag;
            return tag.substring(0, 1).toUpperCase() + tag.substring(1);
        }

        String genresDisplay() {
            return genres.stream().map(Movie::displayTag).collect(Collectors.joining(", "));
        }

        String moodsDisplay() {
            return moods.stream().map(Movie::displayTag).collect(Collectors.joining(", "));
        }
    }

    static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString().trim());
        return fields;
    }

    static List<Movie> loadMovies(String path) {
        List<Movie> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                List<String> fields = parseCsvLine(line);
                if (fields.size() >= 8) {
                    String[] p = new String[8];
                    for (int i = 0; i < 7; i++) p[i] = fields.get(i);
                    p[7] = String.join(",", fields.subList(7, fields.size()));
                    list.add(new Movie(p));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot load " + path);
        }
        return list;
    }

    static List<Movie> recommend(List<Movie> all, int age, String genre,
                                 String mood, String lang, int limit) {
        String[] allowed = age < 13 ? new String[]{"G","U"}
                : age < 18 ? new String[]{"G","PG","U","UA","U/A","PG-13"}
                :            new String[]{"G","PG","U","UA","U/A","PG-13","R","A"};

        Set<String> ok = new HashSet<>(Arrays.asList(allowed));
        String genreKey = genre.trim().toLowerCase();
        String moodKey  = mood.trim().toLowerCase();

        return all.stream()
                .filter(m -> ok.contains(m.rating))
                .filter(m -> m.matchesGenre(genreKey))
                .filter(m -> m.matchesMood(moodKey))
                .filter(m -> lang.equals("All") || m.language.equalsIgnoreCase(lang))
                .sorted(Comparator.comparingDouble((Movie m) -> {
                    double s = m.imdb / 10.0;
                    if (m.language.equalsIgnoreCase(lang)) s += 0.5;
                    return -s;
                }))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Movie> movies = loadMovies("data/movies.csv");

        JFrame f = new JFrame("Movie Recommender");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900, 600);
        f.setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.setPreferredSize(new Dimension(220, 0));

        JTextField ageField = new JTextField();
        String[] moods  = {"Happy","Sad","Excited","Relaxed","Motivated","Scared"};
        String[] genres = {"Action","Comedy","Drama","Horror","Sci-fi","Romance"};
        String[] langs  = {"All","English","Hindi"};
        JComboBox<String> moodBox  = new JComboBox<>(moods);
        JComboBox<String> genreBox = new JComboBox<>(genres);
        JComboBox<String> langBox  = new JComboBox<>(langs);
        JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        JButton btn = new JButton("Find Movies");

        form.add(new JLabel("Age:"));       form.add(ageField);
        form.add(new JLabel("Mood:"));      form.add(moodBox);
        form.add(new JLabel("Genre:"));     form.add(genreBox);
        form.add(new JLabel("Language:")); form.add(langBox);
        form.add(new JLabel("Results:"));  form.add(limitSpinner);
        form.add(new JLabel());             form.add(btn);

        String[] cols = {"Title","Year","Rating","Genre","Mood","Lang","IMDb"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    public boolean isCellEditable(int r, int c) { return false; }
                };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextArea desc = new JTextArea(4, 0);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setText("Select a movie to see its description.");

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        right.add(new JScrollPane(desc),  BorderLayout.SOUTH);

        btn.addActionListener(e -> {
            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
                if (age <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(f, "Enter a valid age.");
                return;
            }
            String genre = ((String) genreBox.getSelectedItem()).toLowerCase();
            String mood  = ((String) moodBox .getSelectedItem()).toLowerCase();
            String lang  = (String) langBox.getSelectedItem();
            int    limit = (int) limitSpinner.getValue();

            List<Movie> results = recommend(movies, age, genre, mood, lang, limit);
            model.setRowCount(0);
            for (Movie m : results)
                model.addRow(new Object[]{m.title, m.year, m.rating,
                        m.genresDisplay(), m.moodsDisplay(), m.language,
                        String.format("%.1f", m.imdb)});

            if (results.isEmpty()) {
                desc.setText("No matches found for " + genre + " + " + mood
                        + (lang.equals("All") ? "" : " (" + lang + ")")
                        + ". Try different filters.");
            } else {
                desc.setText("Showing " + results.size() + " match(es). Click a row for the description.");
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String title = (String) model.getValueAt(row, 0);
            movies.stream()
                    .filter(m -> m.title.equals(title))
                    .findFirst()
                    .ifPresent(m -> desc.setText(m.title + " (" + m.year + ") | "
                            + m.rating + " | IMDb: " + m.imdb + "\n\n" + m.description));
        });

        f.setLayout(new BorderLayout());
        f.add(form,  BorderLayout.WEST);
        f.add(right, BorderLayout.CENTER);
        f.setVisible(true);
    }
}
