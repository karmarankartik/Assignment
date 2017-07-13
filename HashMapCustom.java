package com.karmarankartik;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



/**
 * @author Kartik karmaran
 *  HashMapCustom class
 */

class HashMapCustom<K, V> implements Serializable {

	private static final long serialVersionUID = 1L;

	private Entry<K, V>[] table;
	private int capacity = 16;
	private float loadfactor = 0.75f;
	private static long keyThreshhold;
	private int size;
	static final int MAXIMUM_CAPACITY = 1 << 30;

	// Entry Class for creating Entry Objects
	static class Entry<K, V> implements Serializable {

		private static final long serialVersionUID = 1L;
		final K key;
		V value;
		Entry<K, V> next;

		public Entry(K key, V value, Entry<K, V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}

		@Override
		public String toString() {
			return " [key=" + key + ", value=" + value + "]";
		}

	}

	@SuppressWarnings("unchecked")
	public HashMapCustom() {
		table = new Entry[capacity]; // Creating Entry Type Array for Storing entry objects.
		size = 0;
		keyThreshhold = 1000000; // used as a threshold limit decider to create Backup's.

	}

	// capacity() return's current Capacity of the HashMap.
	public synchronized int capacity() {

		return capacity;
	}

	// size() return's current Size of the HashMap.
	public synchronized int size() {
		return size;
	}

	// IsEmpty() to check the whether the map is empty.
	public synchronized boolean isEmpty() {
		return size == 0;
	}

	// put() to insert key value pair's into the map.
	public synchronized void put(K newKey, V data) {

		// if size of map exceed's the threshold Data is Backed up.Intial value for keyThreshhold is 100000
		if (size > keyThreshhold) {
			spillOnDisk(); // Method responsible for creating Backup.
			keyThreshhold = keyThreshhold + 500000; // Threshold Increased.

		}
		if (size >= loadfactor * capacity) // if 75% of the map is full we resize our Map. resize() method will resize
											// and return the new map.
			table = resize(table);

		if (newKey == null) {
			entryForNull(data); // entryForNull() is called to enter null into the Map only one null is allowed,
								// Null key is always placed at 0th position.

			return;
		}
		int hash = hash(newKey);

		Entry<K, V> newEntry = new Entry<K, V>(newKey, data, null);

		if (table[hash] == null) {
			table[hash] = newEntry;
			size++;

		} else {
			Entry<K, V> previous = null;
			Entry<K, V> current = table[hash];

			while (current != null) {
				if (current.key.equals(newKey)) {
					if (previous == null) {
						newEntry.next = current.next;
						table[hash] = newEntry;

						return;
					} else {
						newEntry.next = current.next;
						previous.next = newEntry;

						return;
					}
				}
				previous = current;
				current = current.next;
			}
			previous.next = newEntry;
			size++;

		}
	}

	private void entryForNull(V value) {

		if (table[0] == null) {
			table[0] = new Entry<K, V>(null, value, null);
			size++;

		} else {
			Entry<K, V> e = table[0];
			e.value = value;

		}

	}

	// Method for resizing the map
	private Entry<K, V>[] resize(Entry<K, V>[] table) {
		capacity = capacity * 2;
		if (capacity > MAXIMUM_CAPACITY)
			capacity = MAXIMUM_CAPACITY;

		@SuppressWarnings("unchecked")
		Entry<K, V>[] newtable = new Entry[capacity];
		for (int i = 0; i < table.length; i++) {
			Entry<K, V> current = table[i];
			while (current != null) {
				int hash = hash(current.key);
				newtable[hash] = current;
				current = current.next;

			}

		}
		return newtable;

	}

	// get() method return's the value for a given key will return null if no such
	// key is present.
	public synchronized V get(K key) {
		int hash = hash(key);

		if (key == null) {
			return getForNullKey();

		} else {
			Entry<K, V> temp = table[hash];
			while (temp != null) {
				if (temp.key.equals(key))
					return temp.value;
				temp = temp.next;
			}
			return null;
		}
	}

	// getForNullKey return the value for null key if present.
	private V getForNullKey() {
		for (Entry<K, V> e = table[0]; e != null; e = e.next) {
			if (e.key == null)
				return e.value;

		}
		return null;
	}

	// Use to remove a key value pair.
	public synchronized boolean remove(K deleteKey) {

		int hash = hash(deleteKey);

		if (deleteKey == null) {
			if (table[0] != null) {
				table[0] = null;
				size--;
				return true;

			} else
				return false;
		} else {
			Entry<K, V> previous = null;
			Entry<K, V> current = table[hash];

			while (current != null) { // we have reached last entry node of bucket.
				if (current.key.equals(deleteKey)) {
					if (previous == null) {
						table[hash] = table[hash].next;
						size--;
						return true;
					} else {
						previous.next = current.next;
						size--;
						return true;
					}
				}
				previous = current;
				current = current.next;
			}
			return false;
		}

	}

	// Used to display all contents of the map.
	public synchronized void display() {

		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				Entry<K, V> entry = table[i];
				while (entry != null) {
					System.out.print("{" + entry.key + "=" + entry.value + "}" + " ");
					entry = entry.next;
				}
			}
		}

	}

	// Used to get collection of all key's present in a map return's an arraylist of
	// key's.
	@SuppressWarnings("rawtypes")
	public synchronized Collection getKeys() {

		List<Object> keys = new ArrayList<Object>();
		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				Entry<K, V> entry = table[i];
				while (entry != null) {
					keys.add(entry.key);
					entry = entry.next;
				}

			}

		}
		return keys;
	}

	// Used to get collection of all value's present in a map return's an arraylist
	// of key's.
	@SuppressWarnings("rawtypes")
	public synchronized Collection getValues() {
		List<Object> values = new ArrayList<Object>();
		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				Entry<K, V> entry = table[i];
				while (entry != null) {
					values.add(entry.value);
					entry = entry.next;
				}

			}

		}
		return values;

	}

	// Used to check whether the key passed is present in the map or not.
	public synchronized boolean containsKey(K key) {
		return get(key) != null;

	}

	// used to check whether a particular value is present in the map or not
	// return's the first instance.
	public synchronized boolean containsValue(V value) {
		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				Entry<K, V> entry = table[i];
				while (entry != null) {
					if (value.equals(entry.value))
						return true;
					entry = entry.next;
				}

			}

		}
		return false;

	}

	// Return's all Entry object in an Arraylist.
	@SuppressWarnings("rawtypes")
	public synchronized Collection EntrySet() {

		List<Entry<K, V>> entryset = new ArrayList<>();

		for (int i = 0; i < capacity; i++) {
			if (table[i] != null) {
				Entry<K, V> entry = table[i];
				while (entry != null) {

					entryset.add(entry);

					entry = entry.next;
				}

			}

		}
		return entryset;
	}

	// Used to clear the Map.
	public synchronized void clear() {

		for (int i = 0; i < capacity; i++) {
			table[i] = null;

		}
		size = 0;
		capacity = 16;

	}

	// used to compute the hashcode and return the bucket location of the entry
	private int hash(K key) {

		return (key == null) ? 0 : (key.hashCode() & capacity - 1);
	}

	// used to create the data backup
	private void spillOnDisk() {
		try {
			FileOutputStream fos = new FileOutputStream("Backup.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(EntrySet());
			oos.close();
			fos.close();
			System.out.printf("Data saved in Backup.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
