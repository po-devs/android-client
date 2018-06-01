    package com.podevs.android.poAndroid.pokeinfo;

    import android.util.SparseArray;
    import com.podevs.android.poAndroid.R;
    import com.podevs.android.poAndroid.pokeinfo.InfoFiller.Filler;
    import com.podevs.android.poAndroid.registry.RegistryActivity;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Comparator;

    public class ItemInfo {
        private static SparseArray<String> itemNames = new SparseArray<String>();
        private static SparseArray<String> itemMessages = null;
        private static int usefulItems[] = null;
        private static int usefulThisGeneration[] = null;
        private static byte Generation = 2;

        public static void setGeneration(byte gen) {
            usefulThisGeneration = null;
            if (gen < 2) {
                gen = 2;
            }
            Generation = gen;
        }

        public static String name(int item) {
            if (itemNames.indexOfKey(item) < 0) {
                loadItemNames();
            }

            return itemNames.get(item, "");
        }

        public static int indexOf(String s) {
            if (itemNames.indexOfKey(1) < 0) {
                loadItemNames();
            }

            for (int i = 0; i < itemNames.size(); i ++) {
                if (itemNames.get(itemNames.keyAt(i), "").equals(s)) {
                    return itemNames.keyAt(i);
                }
            }
            return 15;
        }

        public static int indexOf(int i) {
            return itemNames.keyAt(i);
        }

        public static String message(int num, int part) {
            if (itemMessages == null) {
                 loadItemMessages();
            }

            String parts [] = (itemMessages.get(num, "")).split("\\|");
            try {
                return parts[part];
            } catch (ArrayIndexOutOfBoundsException ex) {
                return "";
            }
        }

        public static int[] getUsefulThisGeneration() {
            if (usefulThisGeneration == null) {
                if (usefulItems == null) {
                    loadUsefulItems();
                }
                loadGenerationItems();
            }
            return usefulThisGeneration;
        }

        private static short plates[] = new short[] {
            0,
            188,
            196,
            201,
            187,
            199,
            192,
            198,
            193,
            189,
            197,
            194,
            202,
            195,
            191,
            185,
            186,
            330,
            0
        };

        public static short plateForType(int type) {
            return plates[type];
        }

        private static short memoryChips[] = new short[] {
            0, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 0 // 344-360
        };

        public static short memoryChipForType(int type) {
            return memoryChips[type];
        }

        private static short zMoves[] = new short[] {
            673, 674, 675, 676, 677, 678, 679, 680, 681, 682, 683, 684, 685, 686, 687,
                688, 689, 690, 691, 692, 693, 694, 695, 696, 697, 698, 699, 700, 701, 702, 703,
                708, 706, 707, 710
        };

        public static short zCrystalMove(short item) {
            if (item < 3000 || item >= 4000) {
                return 0;
            }
            return zMoves[item - 3000];
        }

        private static void loadItemNames() {
            String path;
            if (RegistryActivity.localize_assets) {
                path = "db/items/" + InfoConfig.resources.getString(R.string.asset_localization) + "items.txt";
                if (!InfoConfig.fileExists(path)) {
                    path = "db/items/items.txt";
                }
            } else {
                path = "db/items/items.txt";
            }
            InfoFiller.fill(path, new Filler() {
                public void fill(int i, String b) {
                    itemNames.put(i, b);
                }
            });
            if (RegistryActivity.localize_assets) {
                path = "db/items/" + InfoConfig.resources.getString(R.string.asset_localization) + "berries.txt";
                if (!InfoConfig.fileExists(path)) {
                    path = "db/items/berries.txt";
                }
            } else {
                path = "db/items/berries.txt";
            }
            InfoFiller.fill(path, new Filler() {
                public void fill(int i, String b) {
                    itemNames.put(8000+i, b);
                }
            });
        }

        private static void loadItemMessages() {
            itemMessages = new SparseArray<String>();
            String path;
            if (RegistryActivity.localize_assets) {
                path = "db/items/" + InfoConfig.resources.getString(R.string.asset_localization) + "item_messages.txt";
                if (!InfoConfig.fileExists(path)) {
                    path = "db/item/item_messages.txt";
                }
            } else {
                path = "db/items/item_messages.txt";
            }
            InfoFiller.fill(path, new Filler() {
                public void fill(int i, String b) {
                    itemMessages.put(i, b);
                }
            });
            if (RegistryActivity.localize_assets) {
                path = "db/items/" + InfoConfig.resources.getString(R.string.asset_localization) + "berry_messages.txt";
                if (!InfoConfig.fileExists(path)) {
                    path = "db/items/berry_messages.txt";
                }
            } else {
                path = "db/items/berry_messages.txt";
            }
            InfoFiller.fill(path, new Filler() {
                public void fill(int i, String b) {
                    itemMessages.put(8000+i, b);
                }
            });
        }

        private static void loadUsefulItems() {
            final ArrayList<Integer> items = new ArrayList<Integer>();

            InfoFiller.plainFill("db/items/item_useful.txt", new Filler() {
                public void fill(int i, String s) {
                    items.add(i);
                }
            });

            /* Sort item names */
            Collections.sort(items, new Comparator<Integer>() {
                public int compare(Integer lhs, Integer rhs) {
                    return name(lhs).compareTo(name(rhs));
                }
            });

            final ArrayList<Integer> berries = new ArrayList<Integer>();

            InfoFiller.plainFill("db/items/berry_useful.txt", new Filler() {
                public void fill(int i, String s) {
                    berries.add(8000 + i);
                }
            });

            /* Sort item names */
            Collections.sort(berries, new Comparator<Integer>() {
                public int compare(Integer lhs, Integer rhs) {
                    return name(lhs).compareTo(name(rhs));
                }
            });

            usefulItems = new int[items.size()+berries.size()];

            for (int i = 0; i < items.size(); i++) {
                usefulItems[i] = items.get(i);
            }

            for (int i = 0; i < berries.size(); i++) {
                usefulItems[i+items.size()] = berries.get(i);
            }
        }

        private static boolean primitiveContains(int toFind) {
            for (int i = 0; i < usefulItems.length ; i++) {
                if (usefulItems[i] == toFind) {
                    return true;
                }
            }
            return false;
        }

        private static void loadGenerationItems() {
            final ArrayList<Integer> released_items = new ArrayList<Integer>();

            InfoFiller.plainFill("db/items/" + Generation + "G/released_items.txt", new Filler() {
                public void fill(int i, String s) {
                    released_items.add(i);
                }
            });

            for (int i = released_items.size() - 1; i > -1; i--) {
                if (!primitiveContains(released_items.get(i))) {
                    released_items.remove(released_items.get(i));
                }
            }

            Collections.sort(released_items, new Comparator<Integer>() {
                public int compare(Integer lhs, Integer rhs) {
                    return name(lhs).compareTo(name(rhs));
                }
            });

            final ArrayList<Integer> released_berries = new ArrayList<Integer>();

            InfoFiller.plainFill("db/items/" + Generation + "G/released_berries.txt", new Filler() {
                public void fill(int i, String s) {
                    released_berries.add(8000 + i);
                }
            });

            for (int i = released_berries.size() - 1; i > -1; i--) {
                if (!primitiveContains(released_berries.get(i))) {
                    released_berries.remove(released_berries.get(i));
                }
            }

            Collections.sort(released_berries, new Comparator<Integer>() {
                public int compare(Integer lhs, Integer rhs) {
                    return name(lhs).compareTo(name(rhs));
                }
            });

            usefulThisGeneration = new int[released_items.size() + released_berries.size()];

            for (int i = 0; i < released_items.size(); i++) {
                usefulThisGeneration[i] = released_items.get(i);
            }

            for (int i = 0; i < released_berries.size(); i++) {
                usefulThisGeneration[i+released_items.size()] = released_berries.get(i);
            }
        }

        public static int plateType(short item) {
            for (int i = 0; i < plates.length; i++) {
                if (item == plates[i]) {
                    return i;
                }
            }
            return 0;
        }

        public static int memoryType(short item) {
            for (int i = 0; i < memoryChips.length; i++) {
                if (item == memoryChips[i]) {
                    return i;
                }
            }
            return 0;
        }
    }
