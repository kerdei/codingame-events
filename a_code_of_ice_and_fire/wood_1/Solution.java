import java.util.*;

/*
void (#): not a playable cell.
    neutral (.): doesn't belong to any player.
    captured (O or X): belongs to a player.
    inactive (o or x): belongs to a player but inactive.
 */
enum MapCell {
    VOID('#'),
    NEATURAL('.'),
    CAPTURED_A('O'),
    CAPTURED_B('X'),
    INACTIVE_A('o'),
    INACTIVE_B('x');

    public char tile;

    MapCell(char tile) {
        this.tile = tile;
    }

    public static MapCell getTile(String s) {
        switch (s) {
            case "O":
                return CAPTURED_A;
            case "o":
                return INACTIVE_A;
            case "X":
                return CAPTURED_B;
            case "x":
                return INACTIVE_B;
        }

        return null;
    }
}

class Point {
    int x;
    int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }
}

class Unit {

    int owner;
    int unitId;
    int level;
    Point point;

    public Unit(int owner, int unitId, int level, Point point) {
        this.owner = owner;
        this.unitId = unitId;
        this.level = level;
        this.point = point;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "owner=" + owner +
                ", unitId=" + unitId +
                ", level=" + level +
                ", point=" + point +
                '}';
    }
}

class Building {
    int owner;
    int buildingType;
    Point point;

    public Building(int owner, int buildingType, Point point) {
        this.owner = owner;
        this.buildingType = buildingType;
        this.point = point;
    }

    @Override
    public String toString() {
        return "Building{" +
                "owner=" + owner +
                ", buildingType=" + buildingType +
                ", point=" + point +
                '}';
    }
}


class Player {

    public static final int ENEMY = 1;
    public static final int ME = 0;
    private static Scanner in = new Scanner(System.in);
    private static ArrayList<Unit> myArmy;
    private static ArrayList<Unit> opponentArmy;

    private static ArrayList<Building> myBuildings;

    private static String enemyCellType;

    private static Building opponentHQ;

    private static MapCell[][] map;

    private static StringBuilder output;
    private static int gold;
    private static int income;


    public static void main(String args[]) {

        int numberMineSpots = in.nextInt();
        for (int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }

        // game loop
        while (true) {

            readEconomy();
            readMap();
            readBuildings();
            readArmies();



            /*
            Train to the closest point to the enemy HQ
            army: move to the cloeset gray.
             */
            output = new StringBuilder();
            train();
            orderArmy();

            if (output.length() == 0) {
                output.append("WAIT");
            }

            System.out.println(output);

        }
    }

    private static void readEconomy() {
        gold = in.nextInt();
        income = in.nextInt();
        int opponentGold = in.nextInt();
        int opponentIncome = in.nextInt();
    }

    private static void readBuildings() {
        int buildingCount = in.nextInt();
        myBuildings = new ArrayList<>();
        for (int i = 0; i < buildingCount; i++) {
            int owner = in.nextInt();
            int buildingType = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();

            if (owner == ENEMY) {
                if (buildingType == 0) {
                    opponentHQ = new Building(owner, buildingType, new Point(x, y));
                    enemyCellType = opponentHQ.point.x == 0 ? "o" : "x";
                }

            } else {
                myBuildings.add(new Building(owner, buildingType, new Point(x, y)));
            }
        }
    }

    private static void readArmies() {
        int unitCount = in.nextInt();
        myArmy = new ArrayList<>();
        opponentArmy = new ArrayList<>();
        for (int i = 0; i < unitCount; i++) {
            int owner = in.nextInt();
            Unit unit = new Unit(owner, in.nextInt(), in.nextInt(), new Point(in.nextInt(), in.nextInt()));
            if (owner == ME) {
                myArmy.add(unit);
            } else {
                opponentArmy.add(unit);
            }
        }
    }

    private static void readMap() {
        System.err.println("MAP");
        map = new MapCell[12][12];
        for (int i = 0; i < 12; i++) {
            String line = in.next();
            System.err.println(line);
            char[] chars = line.toCharArray();
            for (int j = 0; j < chars.length; j++) {
                switch (chars[j]) {
                    case '#':
                        map[i][j] = MapCell.VOID;
                        break;
                    case '.':
                        map[i][j] = MapCell.NEATURAL;
                        break;
                    case 'O':
                        map[i][j] = MapCell.CAPTURED_A;
                        break;
                    case 'X':
                        map[i][j] = MapCell.CAPTURED_B;
                        break;
                    case 'o':
                        map[i][j] = MapCell.INACTIVE_A;
                        break;
                    case 'x':
                        map[i][j] = MapCell.INACTIVE_B;
                        break;
                }
            }
        }
        System.err.println();
    }

    private static void orderArmy() {
        if (myArmy.size() > 0) {
            for (Unit unit : myArmy) {

                if (distanceBetweenAB(unit.point, opponentHQ.point) < 1) { // Finishing blow to hq
                    output.append("MOVE ").append(unit.unitId).append(" ").append(opponentHQ.point).append(";");
                    System.err.println(unit.point);
                    System.err.println(opponentHQ.point);
                    System.err.println(distanceBetweenAB(unit.point, opponentHQ.point));
                } else {
                    Point point = closestNotOwnedMapCellPointToPoint(unit.point);
                    if (point != null) {
                        output.append("MOVE ").append(unit.unitId).append(" ").append(point).append(";");
                    }
                }

            }
        }
    }

    private static void train() {
        while (gold >= 10 && income >= 1) {
            Point closestNotOwnedMapCellPointToPoint = closestNotOwnedMapCellPointToPoint(closestCapturablePointToEnemyHQ());
            if (closestNotOwnedMapCellPointToPoint != null) {
                int whatUnitCanWeAfford = whatUnitCanWeAfford(gold, income);

                switch (whatUnitCanWeAfford) {
                    case 1: {
                        gold -= 10;
                        income--;
                        break;
                    }
                    case 2: {
                        gold -= 20;
                        income -= 4;
                        break;
                    }
                    case 3: {
                        gold -= 30;
                        income -= 20;
                        break;
                    }
                }

                output.append("TRAIN ").append(whatUnitCanWeAfford).append(" ").append(closestNotOwnedMapCellPointToPoint).append(";");
                map[closestNotOwnedMapCellPointToPoint.x][closestNotOwnedMapCellPointToPoint.y] = enemyCellType.equalsIgnoreCase("x") ? MapCell.CAPTURED_A : MapCell.CAPTURED_B;
            } else break;

        }
    }

    private static Point closestNotOwnedMapCellPointToPoint(Point closestCapturablePointToEnemyHQ) {

        int min = 23; //Min pos always be 22 max;
        Point closestPoint = new Point(-1, -1); //Invalid pos
        for (int y = closestCapturablePointToEnemyHQ.y - 1; y <= closestCapturablePointToEnemyHQ.y + 1; y++) { // Checking vertically
            if (y == closestCapturablePointToEnemyHQ.y) continue;
            if (y < 0 || y > 11) continue;

            MapCell mapCell = map[closestCapturablePointToEnemyHQ.x][y];
            if (mapCell == MapCell.NEATURAL
                    || mapCell == MapCell.INACTIVE_A
                    || mapCell == MapCell.INACTIVE_B
                    || mapCell == MapCell.getTile(enemyCellType)
                    || mapCell == MapCell.getTile(enemyCellType.toUpperCase())) {
                Point point = new Point(closestCapturablePointToEnemyHQ.x, y);

                int distanceBetweenAB = distanceBetweenAB(point, opponentHQ.point);
                if (distanceBetweenAB < min) {
                    min = distanceBetweenAB;
                    closestPoint = point;
                }
            }
        }

        for (int x = closestCapturablePointToEnemyHQ.x - 1; x <= closestCapturablePointToEnemyHQ.x + 1; x++) { // Checking horizontally
            if (x == closestCapturablePointToEnemyHQ.x) continue;
            if (x < 0 || x > 11) continue;
            MapCell mapCell = map[x][closestCapturablePointToEnemyHQ.y];
            if (mapCell == MapCell.NEATURAL
                    || mapCell == MapCell.INACTIVE_A
                    || mapCell == MapCell.INACTIVE_B
                    || mapCell == MapCell.getTile(enemyCellType)
                    || mapCell == MapCell.getTile(enemyCellType.toUpperCase())) {
                Point point = new Point(x, closestCapturablePointToEnemyHQ.y);

                int distanceBetweenAB = distanceBetweenAB(point, opponentHQ.point);
                if (distanceBetweenAB < min) {
                    min = distanceBetweenAB;
                    closestPoint = point;
                }
            }
        }
        if (closestPoint.x == -1) {
            closestPoint = null;
        }

        return closestPoint;
    }


    private static int whatUnitCanWeAfford(int currentGold, int currentIncome) {
        if (myArmy.size() > 10 && currentGold >= 20 && currentIncome >= 4) {
            return 2;
        } else {
            return 1;
        }
    }

    private static Point closestCapturablePointToEnemyHQ() {
        int min = 23; //Min pos always be 22 max;
        Point minPos = new Point(-1, -1); //Invalid pos

        for (Building building : myBuildings) {
            int distanceBetweenAB = distanceBetweenAB(building.point, opponentHQ.point);
            if (distanceBetweenAB < min) {
                min = distanceBetweenAB;
                minPos = building.point;
            }

        }

        for (Unit unit : myArmy) {
            int distanceBetweenAB = distanceBetweenAB(unit.point, opponentHQ.point);
            if (distanceBetweenAB < min) {
                min = distanceBetweenAB;
                minPos = unit.point;
            }
        }

        if (minPos.x == -1) {
            throw new IllegalStateException("closestCapturablePointToEnemyHQ cannot be found");
        }
        return minPos;
    }


    private static int distanceBetweenAB(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

}