package com.podevs.android.utilities;

public class FixedSizeStack<E> {
    private int size;
    private int max_size;

    private transient Object[] data;

    public FixedSizeStack(int max_size) {
        this.max_size = max_size;
        size = 0;
        data = new Object[]{};
    }

    public synchronized void push(E e) {
        if (size == max_size) {
            System.arraycopy(data, 1, data, 0, size - 1);
            data[size - 1] = e;
        } else {
            Object[] newArray = new Object[size + 1];
            System.arraycopy(data, 0, newArray, 0, size);
            data = newArray;
            data[size] = e;
            size++;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized E get(int index) {
        if (index >= size) {
            return null;
        }
        return (E) data[index];
    }

    @Override
    public synchronized String toString() {
        return "Stack{" + size + ":" + max_size + "}";
    }

    public synchronized String toReadableString() {
        String s = "";
//        for (Object o : data) {
//            s += "\n" + o.toString();
//        }
        for (int i = size - 1; i > -1; i--) {
            s += "\n" + data[i].toString();
        }
        return s;
    }

//    @Override
//    public Iterator<E> iterator() {
//        return new FixedSizeStackIterator();
//    }
//
//    private class FixedSizeStackIterator implements Iterator<E> {
//        private int remaining = size - 1;
//
//        @Override
//        public void remove() {
//            // Stub
//        }
//
//        @Override
//        public boolean hasNext() {
//            return remaining != 0;
//        }
//
//        @Override
//        @SuppressWarnings("unchecked")
//        public E next() {
//            int rem = remaining;
//            remaining = rem - 1;
//            return (E) data[rem];
//        }
//    }
}
