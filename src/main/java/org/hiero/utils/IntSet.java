package org.hiero.utils;

/**
 * A set of integers.
 * A simplified version of IntOpenHash from fastutil http://fastutil.di.unimi.it
 */
public class IntSet {
    // TODO: it would be nice if these were private
    public int[] key; /* The array of the linear probing */
    public int mask;
    public int n;  /* the size of the array - 1 */
    public boolean containsZero = false;  /* zero is reserved to signify an empty cell */
    public int size;

    private int maxFill;
    private final float f; /* the maximal load of the array */

    public IntSet(final int expected, final float f) {
        if ((f > 0.0F) && (f <= 1.0F)) {
            if (expected < 0) {
                throw new IllegalArgumentException("The expected number of elements must be " +
                        "non-negative");
            } else {
                this.f = f;
                this.n = HashUtil.arraySize(expected, f); /* size of array is power of two */
                this.mask = this.n - 1;
                this.maxFill = HashUtil.maxFill(this.n, f);
                this.key = new int[this.n + 1];
            }
        } else {
            throw new IllegalArgumentException("Load factor must be greater than 0 and " +
                    "smaller than or equal to 1");
        }
    }

    public IntSet(final int expected) {
        this(expected, 0.75F);
    }

    public IntSet() {
        this(16, 0.75F);
    }

    private int realSize() {
        return this.containsZero ? (this.size - 1) : this.size;
    }

    /**
     * @param k integer to add to the set
     * @return true if the set changed, false if the item is already in the set
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean add(final int k) {
        if (k == 0) {
            if (this.containsZero) {
                return false;
            }
            this.containsZero = true;
        } else {
            final int[] key = this.key;
            int pos;
            int curr;
            if ((curr = key[pos = HashUtil.murmurHash3(k) & this.mask]) != 0) {
                if (curr == k) {
                    return false;
                }
                while ((curr = key[(pos = (pos + 1) & this.mask)]) != 0) {
                    if (curr == k) {
                        return false;
                    }
                }
            }
            key[pos] = k;
        }
        if (this.size++ >= this.maxFill) {
            this.rehash(HashUtil.arraySize(this.size + 1, this.f));
        }
        return true;
    }

    public boolean contains(final int k) {
        if (k == 0) {
            return this.containsZero;
        } else {
            final int[] key = this.key;
            int curr;
            int pos;
            if((curr = key[pos = HashUtil.murmurHash3(k) & this.mask]) == 0) {
                return false;
            } else if(k == curr) {
                return true;
            } else {
                while((curr = key[(pos = (pos + 1) & this.mask)]) != 0) {
                    if(k == curr) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private void rehash(final int newN) {
        final int[] key = this.key;
        final int mask = newN - 1;
        final int[] newKey = new int[newN + 1];
        int i = this.n;
        int pos;
        for(int j = this.realSize(); j-- != 0; newKey[pos] = key[i]) {
            do {
                --i;
            } while(key[i] == 0);

            if (newKey[pos = HashUtil.murmurHash3(key[i]) & mask] != 0) {
                while (newKey[(pos = (pos + 1) & mask)] != 0) {}
            }
        }
        this.n = newN;
        this.mask = mask;
        this.maxFill = HashUtil.maxFill(this.n, this.f);
        this.key = newKey;
    }

    /**
     * @return a deep copy of IntSet
     */
    public IntSet copy() {
        final IntSet newSet = new IntSet(1, this.f);
        newSet.n = this.n;
        newSet.mask = this.mask;
        newSet.maxFill = this.maxFill;
        newSet.size = this.size;
        newSet.containsZero = this.containsZero;
        newSet.key = new int[this.n + 1];
        System.arraycopy(this.key, 0, newSet.key, 0, this.key.length);
        return newSet;
    }
}

