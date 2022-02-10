package com.company;

import java.util.*;

class Point {
    public final int id;
    public boolean isMarked, isRed;
    public HashSet<Point> connections;

    public Point(int id) {
        this.id = id;
        isMarked = false;
        isRed = false;
        connections = new HashSet<>();
    }

    public boolean connectTo(Point point, boolean passive) {
        if (connections.contains(point))
            return false;

        connections.add(point);
        if (!passive)
            point.connectTo(this, true);

        return true;
    }

    public String getColor() {
        return (isRed) ? "czerwony" : "niebieski";
    }
}

class Graph {
    private final int size;
    private final Point[] points;
    private int edges;

    public Graph(int size) {
        this.size = size;
        edges = 0;

        points = new Point[size];
        for (int i = 0; i < size; i++)
            points[i] = new Point(i);
    }

    public void resetPoints() {
        for (Point point: points)
            point.isMarked = false;
    }

    public boolean connect(int a, int b) {
        if (a == b)
            return false;

        if (points[a-1].connectTo(points[b-1], false)) {
            edges++;
            return true;
        }

        return false;
    }

    public void describeGraph() {
        for (Point point: points) {
            System.out.print("Wierzchołek " + (point.id+1) + ": ");

            for (Point connection: point.connections)
                System.out.print(connection.id+1 + " ");

            System.out.println();
        }

        System.out.println();
    }

    public float getDensity() {
        return (2.0f * Math.abs(edges)) / (Math.abs(size) * Math.abs(size) - 1);
    }

    public boolean isBipartiteBFS(boolean verbose) {
        resetPoints();
        if (verbose)
            System.out.println("--BFS--\n");

        LinkedList<Point> queue = new LinkedList<>();
        StringBuilder path = new StringBuilder();
        int stepCount = 1;
        boolean bipartite = true;

        queue.add(points[0]);
        points[0].isRed = true;
        points[0].isMarked = true;
        while (queue.size() > 0) {
            if (verbose) {
                System.out.println("Krok: " + stepCount);
                path.append(queue.get(0).id+1);
                path.append(" ");
            }

            for (Point connection: queue.get(0).connections) {
                if (!connection.isMarked) {
                    connection.isMarked = true;
                    if (bipartite) {
                        connection.isRed = !queue.get(0).isRed;

                        if (verbose) {
                            System.out.println("Ustawiam kolor wierzchołka " + (connection.id+1) +
                                    " na " + connection.getColor());
                        }
                    }

                    queue.add(connection);
                } else if (bipartite && connection.isRed == queue.get(0).isRed) {
                    if (verbose) {
                        System.out.println("Brak dwudzielności dla " + (connection.id+1) +
                                " jego kolor to " + connection.getColor());
                    }

                    bipartite = false;
                }
            }

            queue.pollFirst();
            if (verbose) {
                System.out.print(path + "w kolejce ");
                for (Point point: queue)
                    System.out.print(point.id+1 + " ");

                System.out.println("\n");
            }

            stepCount++;
        }

        if (verbose && bipartite)
            printColors();

        return bipartite;
    }

    public boolean isBipartiteDFS(boolean verbose) {
        resetPoints();
        if (verbose)
            System.out.println("--DFS--\n");

        Stack<Point> stack = new Stack<>();
        StringBuilder path = new StringBuilder();
        int stepCount = 1;
        boolean bipartite = true;

        stack.add(points[0]);
        points[0].isRed = true;
        points[0].isMarked = true;
        while (stack.size() > 0) {
            Point current = stack.pop();
            if (verbose) {
                System.out.println("Krok: " + stepCount);
                path.append(current.id+1);
                path.append(" ");
            }

            for (Point connection: current.connections) {
                if (!connection.isMarked) {
                    connection.isMarked = true;
                    if (bipartite) {
                        connection.isRed = !current.isRed;

                        if (verbose) {
                            System.out.println("Ustawiam kolor wierzchołka " + (connection.id+1) +
                                    " na " + connection.getColor());
                        }
                    }

                    stack.push(connection);
                } else if (bipartite && connection.isRed == current.isRed) {
                    if (verbose) {
                        System.out.println("Brak dwudzielności dla " + (connection.id+1) +
                                " jego kolor to " + connection.getColor());
                    }

                    bipartite = false;
                }
            }

            if (verbose) {
                System.out.print(path + "na stosie ");
                for (Point point: stack)
                    System.out.print(point.id+1 + " ");

                System.out.println("\n");
            }

            stepCount++;
        }

        if (verbose && bipartite)
            printColors();

        return bipartite;
    }

    private void printColors() {
        for (Point point: points) {
            System.out.println("Wierzchołek " + (point.id+1) + " ma kolor " + point.getColor());
        }
    }
}

public class Main {
    static Graph graph;
    static boolean[][] edges;

    public static boolean makeEdge(int a, int b) {
        edges[a][b] = true;
        edges[b][a] = true;

        return graph.connect(a+1, b+1);
    }

    public static void prepareGraph(int size) {
        graph = new Graph(size);
        edges = new boolean[size][];
        for (int i = 0; i < size; i++) {
            edges[i] = new boolean[size];
            Arrays.fill(edges[i], false);
        }

        for (int i = 0; i < size; i++) {
            if (i != size-1)
                makeEdge(i, i+1);
        }
    }

    public static void ModeA() {
        Scanner scanner = new Scanner(System.in);
        Graph graph;
        int a, b;

        System.out.println("Podaj dane: ");

        String input = scanner.nextLine();
        a = Integer.parseInt(input.split(" ")[0]);
        b = Integer.parseInt(input.split(" ")[1]);

        graph = new Graph(a);
        for (int i = 0; i < b; i++) {
            input = scanner.nextLine();
            int x = Integer.parseInt(input.split(" ")[0]);
            int y = Integer.parseInt(input.split(" ")[1]);

            graph.connect(x, y);
        }

        graph.describeGraph();
        graph.isBipartiteBFS(true);
        graph.isBipartiteDFS(true);
    }

    public static void ModeB() {
        int graphCount = 0, maxGraphCount = 1000;
        int verticesCount = 6;
        int randomEdgeCreationTries = 10;
        // For every interval count how many graphs were present[0] and how many were bipartite[1]
        int[][] counts = new int[][] {
                new int [] {0, 0},
                new int [] {0, 0},
                new int [] {0, 0},
                new int [] {0, 0},
                new int [] {0, 0}
        };
        int[] intervals = new int[] {14, 25, 40, 65};

        Random random = new Random();
        prepareGraph(verticesCount);

        boolean skip = true;
        while (graphCount < maxGraphCount) {
            boolean randomEdgeCreationSuccess = false;

            if (!skip) {
                for (int i = 0; i < randomEdgeCreationTries; i++) {
                    if (makeEdge(random.nextInt(verticesCount), random.nextInt(verticesCount))) {
                        randomEdgeCreationSuccess = true;
                        break;
                    }
                }
            }

            boolean graphIsMaxDensity = true;
            if (!skip && !randomEdgeCreationSuccess) {
                for (int i = 0; i < verticesCount; i++) {
                    if (!graphIsMaxDensity)
                        break;

                    for (int j = 0; j < verticesCount; j++) {
                        if (makeEdge(i, j)) {
                            graphIsMaxDensity = false;
                            break;
                        }
                    }
                }
            } else {
                graphIsMaxDensity = false;
            }

            if (skip || !graphIsMaxDensity) {
                float density = graph.getDensity();
                int firstIndex;
                float densityPercent = density * 100;
                if (densityPercent < intervals[0])
                    firstIndex = 0;
                else if (densityPercent < intervals[1])
                    firstIndex = 1;
                else if (densityPercent < intervals[2])
                    firstIndex = 2;
                else if (densityPercent < intervals[3])
                    firstIndex = 3;
                else
                    firstIndex = 4;

                counts[firstIndex][0] += 1;
                if (graph.isBipartiteBFS(false))
                    counts[firstIndex][1] += 1;

                skip = false;
                graphCount++;
            } else {
                verticesCount++;
                prepareGraph(verticesCount);
                skip = true;
            }
        }

        System.out.println("Gęstość:  Wszystkich/Dwudzielnych:");
        System.out.println("<" + intervals[0] + "%      " + counts[0][0] + "/" + counts[0][1]);
        System.out.println("<" + intervals[1] + "%      " + counts[1][0] + "/" + counts[1][1]);
        System.out.println("<" + intervals[2] + "%      " + counts[2][0] + "/" + counts[2][1]);
        System.out.println("<" + intervals[3] + "%      " + counts[3][0] + "/" + counts[3][1]);
        System.out.println("<100%     " + counts[4][0] + "/" + counts[4][1]);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Wybierz tryb (A|B): ");
        String mode = scanner.nextLine();

        if (mode.equals("A"))
            ModeA();
        else
            ModeB();

    }
}
