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
    CAPTURED('O', 'X'),
    INACTIVE('o', 'x');

    char[] tile;

    MapCell(char... tile) {
        this.tile = tile;
    }
}

class Unit {

    int owner;
    int unitId;
    int level;
    int x;
    int y;

    public Unit(int owner, int unitId, int level, int x, int y) {
        this.owner = owner;
        this.unitId = unitId;
        this.level = level;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "owner=" + owner +
                ", unitId=" + unitId +
                ", level=" + level +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

class Building {
    int owner;
    int buildingType;
    int x;
    int y;

    public Building(int owner, int buildingType, int x, int y) {
        this.owner = owner;
        this.buildingType = buildingType;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Building{" +
                "owner=" + owner +
                ", buildingType=" + buildingType +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

class Player {

    public static final int ENEMY = 1;
    public static final int ME = 0;
    private static Scanner in = new Scanner(System.in);
    private static ArrayList<Unit> myArmy;
    private static ArrayList<Unit> opponentArmy;
    private static Building opponentHQ;


    public static void main(String args[]) {

        int numberMineSpots = in.nextInt();
        for (int i = 0; i < numberMineSpots; i++) {
            int x = in.nextInt();
            int y = in.nextInt();
        }

        // game loop
        while (true) {
            int gold = in.nextInt();
            int income = in.nextInt();
            int opponentGold = in.nextInt();
            int opponentIncome = in.nextInt();
            for (int i = 0; i < 12; i++) {
                String line = in.next();
            }
            int buildingCount = in.nextInt();
            for (int i = 0; i < buildingCount; i++) {
                int owner = in.nextInt();
                int buildingType = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();

                if (owner == ENEMY) {
                    opponentHQ = new Building(owner, buildingType, x, y);
                }
            }
            int unitCount = in.nextInt();
            myArmy = new ArrayList<>();
            opponentArmy = new ArrayList<>();
            for (int i = 0; i < unitCount; i++) {
                int owner = in.nextInt();
                Unit unit = new Unit(owner, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
                if (owner == ME) {
                    myArmy.add(unit);
                } else {
                    opponentArmy.add(unit);
                }
            }

            System.err.println(opponentHQ);

            StringBuilder output = new StringBuilder();


            if (myArmy.size() > 0) {
                for (Unit unit : myArmy) {
                    output.append("MOVE ").append(unit.unitId).append(" ").append(opponentHQ.x).append(" ").append(opponentHQ.y).append(";");
                }
            }

            if (gold >= 10 && income > 0) {
                if (opponentHQ.x == 11 && opponentHQ.y == 11) {
                    output.append("TRAIN 1 1 0;");

                } else {
                    output.append("TRAIN 1 10 11;");
                }
            } else {
                output.append("WAIT");
            }
            System.out.println(output);

        }
    }
}