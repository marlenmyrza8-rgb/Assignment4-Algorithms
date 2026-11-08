import java.util.*;
/** Implement these algorithms. docs/guide/CONTRACTS.md defines each field and limit. */
public final class Algorithms {
    /**
     * Compare two versions of a sequence while preserving the order of shared characters. Implement lcs according to docs/guide/CONTRACTS.md.
     */

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");

        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        StringBuilder sequence = new StringBuilder();
        int i = n, j = m;

        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sequence.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("length", dp[n][m]);
        result.put("sequence", sequence.reverse().toString());
        return result;
    }

    /**
     * Plan the cheapest sequence of adjacent block merges when the original order must be preserved. Implement merge according to docs/guide/CONTRACTS.md.
     */

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();

        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i]
                    + ((Number) sizes.get(i)).longValue();
        }

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len <= n; l++) {
                int r = l + len - 1;
                dp[l][r] = Long.MAX_VALUE;

                for (int k = l; k < r; k++) {
                    long cost = dp[l][k]
                            + dp[k + 1][r]
                            + prefix[r + 1] - prefix[l];

                    if (cost < dp[l][r]) {
                        dp[l][r] = cost;
                        split[l][r] = k;
                    }
                }
            }
        }

        List<List<Integer>> plan = new ArrayList<>();

        if (n > 1) {
            buildMergePlan(0, n - 1, split, plan);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", n == 0 ? 0L : dp[0][n - 1]);
        result.put("plan", plan);
        return result;
    }

    static void buildMergePlan(
            int l, int r, int[][] split,
            List<List<Integer>> plan) {

        if (l >= r) return;

        int k = split[l][r];

        buildMergePlan(l, k, split, plan);
        buildMergePlan(k + 1, r, split, plan);

        plan.add(Arrays.asList(l, k, r));
    }

    /**
     * Choose a maximum-value set when neighbouring vertices are incompatible and conflicts form a tree. Implement tree according to docs/guide/CONTRACTS.md.
     */

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<?> weights = (List<?>) input.get("weights");
        List<?> edges = (List<?>) input.get("edges");

        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (Object edge : edges) {
            List<?> e = (List<?>) edge;
            int u = ((Number) e.get(0)).intValue();
            int v = ((Number) e.get(1)).intValue();
            graph.get(u).add(v);
            graph.get(v).add(u);
        }

        long[] take = new long[n];
        long[] skip = new long[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        int[] order = new int[n];
        int count = 0;

        if (n > 0) {
            parent[0] = 0;
            order[count++] = 0;
        }

        for (int i = 0; i < count; i++) {
            int u = order[i];
            for (int v : graph.get(u)) {
                if (v == parent[u]) continue;
                parent[v] = u;
                order[count++] = v;
            }
        }

        for (int i = count - 1; i >= 0; i--) {
            int u = order[i];
            take[u] = ((Number) weights.get(u)).longValue();

            for (int v : graph.get(u)) {
                if (parent[v] == u) {
                    take[u] += skip[v];
                    skip[u] += Math.max(take[v], skip[v]);
                }
            }
        }

        List<Integer> selected = new ArrayList<>();
        boolean[] chosen = new boolean[n];

        for (int i = 0; i < count; i++) {
            int u = order[i];

            if (u == 0 || !chosen[parent[u]]) {
                chosen[u] = take[u] > skip[u];
            }

            if (chosen[u]) {
                selected.add(u);
            }
        }

        Collections.sort(selected);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weight", n == 0 ? 0L
                : Math.max(take[0], skip[0]));
        result.put("selected", selected);
        return result;
    }

    /**
     * Establish a simple exact-search baseline against which the other two searches can be checked. Implement naive according to docs/guide/CONTRACTS.md.
     */
    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        for (int i = 0; i <= n - m; i++) {
            int j = 0;

            while (j < m &&
                    text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                positions.add(i);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positions", positions);
        return result;
    }

    /**
     * Reuse information about matched prefixes to avoid restarting the search after every mismatch. Implement kmp according to docs/guide/CONTRACTS.md.
     */
    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        // Empty pattern matches at every boundary
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("positions", positions);
            return result;
        }

        // Build prefix function
        int[] pi = new int[m];

        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];

            while (j > 0 &&
                    pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            pi[i] = j;
        }

        // Search for all occurrences
        int j = 0;

        for (int i = 0; i < n; i++) {
            while (j > 0 &&
                    text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                positions.add(i - m + 1);
                j = pi[j - 1];
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positions", positions);
        return result;
    }

    /**
     * Reject windows with unequal fingerprints and verify every equal-hash candidate. Implement rk according to docs/guide/CONTRACTS.md.
     */
    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }
        } else if (m <= n) {
            final int BASE = 31;
            final int MOD = 101;

            int patternHash = 0;
            int windowHash = 0;
            int highestPower = 1;

            for (int i = 1; i < m; i++) {
                highestPower = (highestPower * BASE) % MOD;
            }

            for (int i = 0; i < m; i++) {
                patternHash = (patternHash * BASE
                        + pattern.charAt(i)) % MOD;

                windowHash = (windowHash * BASE
                        + text.charAt(i)) % MOD;
            }

            for (int i = 0; i <= n - m; i++) {
                if (patternHash == windowHash) {
                    boolean match = true;

                    for (int j = 0; j < m; j++) {
                        if (text.charAt(i + j) != pattern.charAt(j)) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        positions.add(i);
                    }
                }

                if (i < n - m) {
                    int outgoing = text.charAt(i);
                    int incoming = text.charAt(i + m);

                    windowHash = (windowHash
                            - outgoing * highestPower % MOD
                            + MOD) % MOD;

                    windowHash = (windowHash * BASE
                            + incoming) % MOD;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positions", positions);
        return result;
    }

    /**
     * Order the text suffixes and use adjacent common prefixes to find repeated substrings. Implement suffix according to docs/guide/CONTRACTS.md.
     */
    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();

        int[] sa = new int[n];
        int[] rank = new int[n];
        int[] temp = new int[n];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }

        // Prefix doubling with radix/counting sort.
        int[] buffer = new int[n];
        int[] count = new int[Math.max(n + 2, 65538)];

        int classes = 65536;

        for (int k = 1; k < n; k *= 2) {
            // Sort by second rank.
            Arrays.fill(count, 0, classes + 2, 0);

            for (int i = 0; i < n; i++) {
                int key = i + k < n ? rank[i + k] + 1 : 0;
                count[key]++;
            }

            for (int i = 1; i < classes + 2; i++) {
                count[i] += count[i - 1];
            }

            for (int i = n - 1; i >= 0; i--) {
                int key = i + k < n ? rank[i + k] + 1 : 0;
                buffer[--count[key]] = i;
            }

            // Sort by first rank.
            Arrays.fill(count, 0, classes + 2, 0);

            for (int i = 0; i < n; i++) {
                count[rank[buffer[i]] + 1]++;
            }

            for (int i = 1; i < classes + 2; i++) {
                count[i] += count[i - 1];
            }

            for (int i = n - 1; i >= 0; i--) {
                int pos = buffer[i];
                int key = rank[pos] + 1;
                sa[--count[key]] = pos;
            }

            // Assign new ranks.
            int newClasses = 1;
            temp[sa[0]] = 0;

            for (int i = 1; i < n; i++) {
                int a = sa[i - 1];
                int b = sa[i];

                int a2 = a + k < n ? rank[a + k] : -1;
                int b2 = b + k < n ? rank[b + k] : -1;

                if (rank[a] != rank[b] || a2 != b2) {
                    newClasses++;
                }

                temp[b] = newClasses - 1;
            }

            System.arraycopy(temp, 0, rank, 0, n);
            classes = newClasses;

            if (classes == n) break;
        }

        // Kasai LCP.
        int[] inverse = new int[n];
        int[] lcp = new int[n];

        for (int i = 0; i < n; i++) {
            inverse[sa[i]] = i;
        }

        int h = 0;

        for (int i = 0; i < n; i++) {
            int r = inverse[i];

            if (r == 0) {
                h = 0;
                continue;
            }

            int j = sa[r - 1];

            while (i + h < n && j + h < n
                    && text.charAt(i + h) == text.charAt(j + h)) {
                h++;
            }

            lcp[r] = h;
            if (h > 0) h--;
        }

        int bestLength = 0;
        int bestStart = 0;

        for (int i = 1; i < n; i++) {
            if (lcp[i] > bestLength) {
                bestLength = lcp[i];
                bestStart = sa[i];
            }
        }

        String repeated = text.substring(
                bestStart, bestStart + bestLength
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sa", sa);
        result.put("lcp", lcp);
        result.put("repeated", repeated);

        return result;
    }

    /**
     * Build one dictionary automaton and count its patterns in several independent texts. Implement aho according to docs/guide/CONTRACTS.md.
     */
    static Map<String, Object> aho(Map<String, Object> input) {
        List<String> patterns = (List<String>) input.get("patterns");
        List<String> texts = (List<String>) input.get("texts");

        final int ALPHABET = 95;

        // Trie transitions. ASCII characters 32..126.
        List<int[]> transitions = new ArrayList<>();
        List<Integer> failures = new ArrayList<>();

        transitions.add(new int[ALPHABET]);
        failures.add(0);

        int[] terminal = new int[patterns.size()];

        // Build trie, sharing terminal states for duplicates.
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            int state = 0;

            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;

                if (transitions.get(state)[c] == 0) {
                    int next = transitions.size();
                    transitions.get(state)[c] = next;

                    transitions.add(new int[ALPHABET]);
                    failures.add(0);
                }

                state = transitions.get(state)[c];
            }

            terminal[i] = state;
        }

        int states = transitions.size();
        int[] fail = new int[states];

        // BFS order for failure-link propagation.
        int[] order = new int[states];
        int head = 0;
        int tail = 0;

        int[] root = transitions.get(0);

        for (int c = 0; c < ALPHABET; c++) {
            int child = root[c];

            if (child != 0) {
                fail[child] = 0;
                order[tail++] = child;
            }
        }

        // Build failure links and complete transitions.
        while (head < tail) {
            int v = order[head++];
            int[] edges = transitions.get(v);
            int[] fallback = transitions.get(fail[v]);

            for (int c = 0; c < ALPHABET; c++) {
                int u = edges[c];

                if (u != 0) {
                    fail[u] = fallback[c];
                    order[tail++] = u;
                } else {
                    edges[c] = fallback[c];
                }
            }
        }

        List<List<Long>> allCounts = new ArrayList<>();

        // Process each text independently.
        for (String text : texts) {
            long[] visits = new long[states];
            int state = 0;

            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i) - 32;
                state = transitions.get(state)[c];
                visits[state]++;
            }

            // Propagate counts along failure links.
            for (int i = tail - 1; i >= 0; i--) {
                int v = order[i];
                visits[fail[v]] += visits[v];
            }

            List<Long> row = new ArrayList<>(patterns.size());

            for (int i = 0; i < patterns.size(); i++) {
                row.add(visits[terminal[i]]);
            }

            allCounts.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("counts", allCounts);

        return result;
    }
}
