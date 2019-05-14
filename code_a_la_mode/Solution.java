import java.util.*;


class Player {

    private static ArrayList<String> kitchenMap;
    private static ArrayList<Table> tables = new ArrayList<>();

    private static Chef partnerChef;
    private static Chef myChef;

    private final static int DISH_COUNT = 3;
    private static String ovenContents = "";


    private static Scanner in = new Scanner(System.in);
    private static ArrayList<String> orders;

    public static void main(String[] args) {

        storeMapValues();

        myChef = new Chef();
        partnerChef = new Chef();
        orders = new ArrayList<>();
        // game loop
        while (true) {

            turnInitialize();
            searchPreparedFoodOnTables();

            // If order isn't up we should put the thing from our hand
            if (!myChef.items.isEmpty() && !isOrderStillUp(myChef, orders)) {
                dropAtFreeTable();
            }

            useDishWasherWhenThereIsNoMoreFreeDish();

            if (myChef.status == Status.PICKING_ORDER) {
                pickOrder(partnerChef);
            }

            if (myChef.status == Status.PREPARATION) {
                prepareFood();
            }
            if (myChef.status == Status.MAKING_DISH) {
                makeDish();
            }
            deliverDish();

            System.err.println("LOG:");
            System.err.println(myChef.items);
            System.err.println(myChef.currentOrder);
            System.err.println(myChef.status);
            System.out.println(myChef.action);

            tables = new ArrayList<>(); //clear
            orders = new ArrayList<>(); //clear
        }
    }

    private static void useDishWasherWhenThereIsNoMoreFreeDish() {
        if ((myChef.status == Status.MAKING_DISH || myChef.status == Status.DISHWASHING) && !myChef.items.equals("DISH")) {
            int count = 0;
            Point lastTablePos = new Point();
            for (Table table : tables) {
                if (table.item.contains("DISH")) {
                    lastTablePos = table.pos;
                    count++;
                }
            }

            if (count >= DISH_COUNT || myChef.status == Status.DISHWASHING) {
                myChef.status = Status.DISHWASHING;
                if (myChef.items.equals("NONE")) {
                    useOrMoveAtPoint(lastTablePos);
                } else {
                    useOrMoveAtKitchenPart(KitchenPart.THE_DISHWASHER);
                }
            }
        }
    }


    private static void pickOrder(Chef partnerChef) {
        //TODO this can be more advanced.
        for (String order : orders) {
            if (!partnerChef.items.equals(order)) {
                myChef.currentOrder = order;
                myChef.status = Status.PREPARATION;
            }
        }
    }

    private static void prepareFood() {

        if (isEverythingPrepared()) {
            System.err.println("EVERYTHING IS PREPARED TIME TO MAKE THE DISH");
            myChef.status = Status.MAKING_DISH;
            return;
        }

        String[] orderFoods = myChef.currentOrder.split("-");
        for (String orderFood : orderFoods) {
            System.err.println(orderFood);
            Food food = Food.valueOf(orderFood);


            if (food.ingredients.length > 0) { //which means it needs to be chopped or baked
                System.err.println("Its time to chop or bake");
                Food foodIngredient = food.ingredients[0]; //TODO 2 ingredient food

                if (myChef.items.contains(food.name()) || myChef.finishedPreparingFood(food)) {
                    if (myChef.items.contains(food.name())) {
                        dropAtFreeTable();
                        break;
                    }
                } else {
                    if (food == Food.CHOPPED_STRAWBERRIES || food == Food.TART) {
                        chop(foodIngredient);
                        break;
                    }
                    if (foodIngredient.ovenable) {
                        bake(food);
                        break;
                    }
                }
            }
        }
    }


    private static void bake(Food food) {

        Food foodIngredient = food.ingredients[0];
        if (ovenContents.equals("NONE")) {
            putIntoOvenOrPickUpFood(foodIngredient);
        } else {
            if (ovenContents.equals(foodIngredient.name())) {
                if (myChef.items.equals("NONE")) {
                    if (isChefNearKitchenPart(KitchenPart.THE_OVEN)) {
                        waitChef();
                    } else {
                        moveChiefToKitchenPart(KitchenPart.THE_OVEN);
                    }
                } else {
                    dropAtFreeTable();//TODO DROP NEAR
                }

            } else {
                if (ovenContents.equals(food.name())) {
                    if (myChef.items.equals("NONE")) {
                        useOrMoveAtKitchenPart(KitchenPart.THE_OVEN);
                    } else {
                        dropAtFreeTable();
                    }
                }
            }
        }
    }

    private static void putIntoOvenOrPickUpFood(Food foodIngredient) {
        if (myChef.items.contains(foodIngredient.name())) {
            useOrMoveAtKitchenPart(KitchenPart.THE_OVEN);
        } else {
            useOrMoveAtKitchenPart(foodIngredient.foodCrates);
        }
    }

    private static void chop(Food foodIngredient) {
        if (myChef.items.contains(foodIngredient.name())) {
            if (isChefNearKitchenPart(KitchenPart.CHOPPING_BOARD)) {
                useAtKitchenPart(KitchenPart.CHOPPING_BOARD);
                if (foodIngredient.name().equals(Food.DOUGH.name())) {
                    myChef.wasDoughChop = true;
                }
                System.err.println(myChef.action);
            } else {
                moveChiefToKitchenPart(KitchenPart.CHOPPING_BOARD);
            }
        } else {
            useOrMoveAtKitchenPart(foodIngredient.foodCrates);
        }
    }

    private static void makeDish() {
        if (isEverythingOnTheDish(myChef)) {
            myChef.status = Status.DELIVERING;
        } else {
            if (isEverythingPrepared()) {
                if (myChef.items.contains("DISH")) {
                    //Go for preparedFood
                    if (!myChef.preparedFood.isEmpty()) {
                        //TODO closest
                        Point pos = myChef.preparedFood.get(0).pos;
                        if (isChefNearPoint(pos)) {
                            useAtPoint(pos);
                            myChef.preparedFood.remove(0);
                        } else {
                            moveChiefToPoint(pos);
                        }
                    } else {
                        String[] currentOrderFoods = myChef.currentOrder.split("-");
                        for (String currentOrderFood : currentOrderFoods) {
                            Food food = Food.valueOf(currentOrderFood);
                            if (!myChef.items.contains(food.name()) && food.ingredients.length == 0) {
                                useOrMoveAtKitchenPart(food.foodCrates);
                                break;
                            }

                        }
                    }
                } else {
                    useOrMoveAtKitchenPart(KitchenPart.THE_DISHWASHER);
                }
            } else {
                myChef.status = Status.PREPARATION;
            }
        }
    }

    private static boolean isOrderStillUp(Chef chef, ArrayList<String> orders) {
        String currentOrder = chef.currentOrder;
        boolean wasFound = false;
        for (String order : orders) {
            if (currentOrder.equals(order)) {
                wasFound = true;
            }
            if (wasFound) {
                return true;
            }
        }
        return false;
    }


    private static void searchPreparedFoodOnTables() {
        myChef.preparedFood = new ArrayList<>();
        for (Table table : tables) {
            myChef.preparedFood.add(new PreparedIngredient(table.item, table.pos));
        }
    }

    private static void deliverDish() {
        if (myChef.status == Status.DELIVERING) {
            if (isChefNearKitchenPart(KitchenPart.THE_WINDOW)) {
                useAtKitchenPart(KitchenPart.THE_WINDOW);
                myChef.status = Status.PICKING_ORDER;
            } else {
                moveChiefToKitchenPart(KitchenPart.THE_WINDOW);
            }
        }
    }

    private static boolean isEverythingOnTheDish(Chef chef) {
        List<String> chefFoods = Arrays.asList(chef.items.split("-"));
        String[] orderFoods = chef.currentOrder.split("-");

        for (String food : orderFoods) {
            if (!chefFoods.contains(food)) {
                return false;
            }
        }
        return true;
    }

    // If there is a orderFood which is not prepared yet then it will return false
    private static boolean isEverythingPrepared() {

        ArrayList<String> orderFoods = new ArrayList<>(Arrays.asList(myChef.currentOrder.split("-")));
        ArrayList<String> chiefPreparedFoods = new ArrayList<>();

        for (PreparedIngredient preparedFoods : myChef.preparedFood) { // Food that on the tables
            chiefPreparedFoods.add(preparedFoods.food);
        }

        chiefPreparedFoods.addAll(Arrays.asList(myChef.items.split("-"))); // Food that in the chief's hand

        for (String orderFood : orderFoods) {

            // We only need to check foods that not ready when you pick them up from create
            if (Food.valueOf(orderFood).ingredients.length > 0) {
                boolean wasFound = false;

                if (chiefPreparedFoods.contains(orderFood)) {
                    wasFound = true;
                }

                if (!wasFound) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void waitChef() {
        if (!myChef.actionGivenThisTurn) {
            myChef.action = "WAIT; WAIT";
        }
    }


    private static void dropAtFreeTable() {
        for (int i = 0; i < kitchenMap.size(); i++) {
            for (int j = 0; j < kitchenMap.get(i).length(); j++) {
                if (kitchenMap.get(i).charAt(j) == KitchenPart.EMPTY_TABLE.tileChar) {
                    // x == j, y == i
                    Point emptyTablePoint = new Point(j, i);
                    if (isChefNearPoint(emptyTablePoint) && isTableFree(emptyTablePoint)) {
                        useAtPoint(emptyTablePoint);
                        return;
                    } else {
                        moveChiefToPoint(emptyTablePoint);
                        //Dont put return here.
                    }
                }
            }
        }
    }

    private static boolean isTableFree(Point emptyTablePoint) {
        for (Table table : tables) {
            if (table.pos.x == emptyTablePoint.x && table.pos.y == emptyTablePoint.y)
                return false;
        }
        return true;
    }

    private static void useOrMoveAtKitchenPart(KitchenPart kitchenPart) {
        if (isChefNearKitchenPart(kitchenPart)) {
            useAtKitchenPart(kitchenPart);
        } else {
            moveChiefToKitchenPart(kitchenPart);
        }
    }

    private static void useOrMoveAtKitchenPart(KitchenPart kitchenPart, Status status) {
        if (isChefNearKitchenPart(kitchenPart)) {
            useAtKitchenPart(kitchenPart);
            myChef.status = status;
        } else {
            moveChiefToKitchenPart(kitchenPart);
        }
    }

    private static void useOrMoveAtPoint(Point point) {
        if (isChefNearPoint(point)) {
            System.err.println("b");
            useAtPoint(point);
        } else {
            moveChiefToPoint(point);
        }
    }

    private static void useAtKitchenPart(KitchenPart kitchenPart) {
        if (!myChef.actionGivenThisTurn) {
            Point kitchenPartPoint = getKitchenPartPoint(kitchenPart);
            myChef.action = "USE " + kitchenPartPoint.x + " " + kitchenPartPoint.y + "; useAtKitchenPart " + kitchenPart.name();
        }
    }

    private static void useAtPoint(Point point) {
        if (!myChef.actionGivenThisTurn) {
            myChef.action = "USE " + point.x + " " + point.y + "; useAtPoint " + point;
        }
    }

    private static boolean isChefNearKitchenPart(KitchenPart kitchenPart) {
        Point point = getKitchenPartPoint(kitchenPart);
        point = getNeighborWalkableCellPoint(point);
        return (myChef.pos.x == point.x && myChef.pos.y == point.y);
    }

    private static boolean isChefNearPoint(Point point) {
        point = getNeighborWalkableCellPoint(point);
        return (myChef.pos.x == point.x && myChef.pos.y == point.y);
    }

    private static void moveChiefToKitchenPart(KitchenPart kitchenPart) {
        if (!myChef.actionGivenThisTurn) {
            Point point = getKitchenPartPoint(kitchenPart);
            point = getNeighborWalkableCellPoint(point);
            myChef.action = "MOVE " + point.x + " " + point.y + "; moveChiefToKitchenPart " + kitchenPart.name();
        }
    }

    private static void moveChiefToPoint(Point point) {
        if (!myChef.actionGivenThisTurn) {
            point = getNeighborWalkableCellPoint(point);
            myChef.action = "MOVE " + point.x + " " + point.y + "; moveChiefToPoint " + point;
        }
    }

    private static Point getKitchenPartPoint(KitchenPart kitchenPart) {
        for (int i = 0; i < kitchenMap.size(); i++) {
            for (int j = 0; j < kitchenMap.get(i).length(); j++) {
                if (kitchenMap.get(i).charAt(j) == kitchenPart.tileChar) {
                    return new Point(j, i);
                }
            }
        }
        System.err.println("something not gut"); //TODO
        return new Point();
    }

    private static Point getNeighborWalkableCellPoint(final Point kitchenPartLocation) {

        Point walkablePoint = new Point();


        for (int i = kitchenPartLocation.y - 1; i <= kitchenPartLocation.y + 1; i++) {

            if (i < 0) continue;
            if (i == kitchenMap.size()) continue;

            for (int j = kitchenPartLocation.x - 1; j <= kitchenPartLocation.x + 1; j++) {
                if (j < 0) continue;
                if (j == kitchenMap.get(i).length()) continue;
                if (j == kitchenPartLocation.x && i == kitchenPartLocation.y) continue;

                if ((kitchenMap.get(i).charAt(j) == KitchenPart.WALKABLE_CELL.tileChar
                        || kitchenMap.get(i).charAt(j) == KitchenPart.FIRST_PLAYER_SPAWN.tileChar
                        || kitchenMap.get(i).charAt(j) == KitchenPart.SECOND_PLAYER_SPAWN.tileChar)
                        &&
                        !(j == partnerChef.pos.x && i == partnerChef.pos.y)
                ) {
                    walkablePoint = new Point(j, i);
                }
            }
        }
        return walkablePoint;
    }

    private static void turnInitialize() {
        myChef.actionGivenThisTurn = false;
        int turnsRemaining = in.nextInt();

        myChef.pos.x = in.nextInt();
        myChef.pos.y = in.nextInt();
        myChef.items = in.next();

        partnerChef.pos.x = in.nextInt();
        partnerChef.pos.y = in.nextInt();
        partnerChef.items = in.next();


        int numTablesWithItems = in.nextInt(); // the number of tables in the kitchen that currently hold an items
        for (int i = 0; i < numTablesWithItems; i++) {
            int tableX = in.nextInt();
            int tableY = in.nextInt();
            String item = in.next();
            tables.add(new Table(item, new Point(tableX, tableY)));
        }
        ovenContents = in.next();
        int ovenTimer = in.nextInt();

        int numCustomers = in.nextInt(); // the number of customers currently waiting for food
        for (int i = 0; i < numCustomers; i++) {
            orders.add(in.next());
            int customerAward = in.nextInt();
        }
    }

    private static void storeMapValues() {
        int numAllCustomers = in.nextInt();
        for (int i = 0; i < numAllCustomers; i++) {
            String customerItem = in.next();
            int customerAward = in.nextInt(); // the number of points awarded for delivering the food
        }
        kitchenMap = new ArrayList<>();
        in.nextLine(); // it's in the default code as well
        for (int i = 0; i < 7; i++) {
            kitchenMap.add(in.nextLine());
        }
    }


    enum Status {
        PREPARATION,
        CHOPING,
        MAKING_DISH,
        PICKING_ORDER,
        BAKING,
        DISHWASHING,
        DELIVERING
    }

    enum Food {
        DISH(false, false, KitchenPart.THE_DISHWASHER),
        ICE_CREAM(false, false, KitchenPart.ICE_CREAM_CREATE),
        BLUEBERRIES(false, false, KitchenPart.BLUEBERRIE_CREATE),
        STRAWBERRIES(false, true, KitchenPart.STRAWBERRIE_CREATE),
        CHOPPED_STRAWBERRIES(false, false, null, STRAWBERRIES),
        DOUGH(true, true, KitchenPart.DOUGH_CRATE),
        CHOPPED_DOUGH(false, false, null),
        CROISSANT(false, false, null),
        RAW_TART(true, false, null),
        TART(false, false, null);

        private boolean chopable;
        private boolean ovenable;
        private KitchenPart foodCrates; //Where can the chief pick it up
        private Food[] ingredients;

        Food(boolean chopable, boolean ovenable, KitchenPart foodCrates, Food... ingredients) {
            this.chopable = chopable;
            this.ovenable = ovenable;
            this.foodCrates = foodCrates;
            this.ingredients = ingredients;
        }
    }

    enum KitchenPart {
        EMPTY_TABLE('#'),
        CHOPPING_BOARD('C'),
        THE_WINDOW('W'),
        THE_DISHWASHER('D'),
        WALKABLE_CELL('.'),
        BLUEBERRIE_CREATE('B'),
        ICE_CREAM_CREATE('I'),
        STRAWBERRIE_CREATE('S'),
        DOUGH_CRATE('H'),
        THE_OVEN('O'),
        FIRST_PLAYER_SPAWN('0'),
        SECOND_PLAYER_SPAWN('1');

        final char tileChar;

        KitchenPart(char tileChar) {
            this.tileChar = tileChar;
        }
    }
}

class Point {
    int x;
    int y;

    Point() {
    }

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x:" + x + " y:" + y;
    }
}

class PreparedIngredient {
    Point pos;
    String food;

    PreparedIngredient(String food, Point pos) {
        this.pos = pos;
        this.food = food;
    }
}

class Table {

    String item;
    Point pos;

    Table(String item, Point pos) {
        this.item = item;
        this.pos = pos;
    }
}

class Chef {

    boolean wasDoughChop;
    Point pos;
    String items;
    String action;
    String currentOrder;
    ArrayList<PreparedIngredient> preparedFood;
    Player.Status status;
    boolean actionGivenThisTurn;

    Chef() {
        actionGivenThisTurn = false;
        pos = new Point();
        currentOrder = "";
        preparedFood = new ArrayList<>();
        status = Player.Status.PICKING_ORDER;
        wasDoughChop = false;
    }

    boolean finishedPreparingFood(Player.Food food) {
        for (PreparedIngredient alreadyDoneIngredient : preparedFood) {
            if (alreadyDoneIngredient.food.contains(food.name())) {
                return true;
            }
        }
        return false;
    }
}

