package org.benchmarx.emf.diff;

public final class TextDiffRenderer {

    private static final String SEPARATOR = "─".repeat(72);

    public String render(ModelDiff diff) {
        if (diff.isEmpty()) return "No differences found.";

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(SEPARATOR).append("\n");
        sb.append("  MODEL MISMATCH — ")
          .append(diff.size())
          .append(" difference(s) found\n");
        sb.append(SEPARATOR).append("\n");

        for (int i = 0; i < diff.entries().size(); i++) {
            sb.append(String.format("  [%02d] %s%n", i + 1,
                                    diff.entries().get(i).describe()));
        }

        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }
}
