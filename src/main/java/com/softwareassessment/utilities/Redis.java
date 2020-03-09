package com.softwareassessment.utilities;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.sortedset.ZAddParams;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;



public class Redis {

    private static Jedis jedis = new Jedis("localhost");

    public static boolean login(String username, String password) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        //Change the byte[] from MessageDigest into a string
        String hashPass = Base64.getEncoder().encodeToString(digest.digest(password.getBytes()));
        if(jedis.hexists("user:" + username, "password")) {
            return (jedis.hget("user:" + username, "password").equals(hashPass));
        } else {
            return false;
        }
    }

   //To register User
    public static boolean registerUser(String username, String password) {

        if(!jedis.exists("user:" + username)) {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
            //Change the byte[] from MessageDigest into a string
            String hashPass = Base64.getEncoder().encodeToString(digest.digest(password.getBytes()));
            jedis.hset("user:" + username, "password", hashPass);
            return true;
        } else {
            return false;
        }
    }

    // Fetches all data associated with a user

    public static User getUserData(String username) {
     //   auth();
        User user = new User();
        user.setUsername(username);
        user.setEntries(getUserEntries(user));
        return user;
    }

    //Fetches a users address book entries

    public static Entry[] getUserEntries(User user) {

        String listKey = user.getUsername() + ":contactList";
        //Ensure that they have any entries
        if (jedis.exists(listKey)) {
            //Redis range -1 acquires all values under the key in the list
            String[] entryKeys = jedis.zrange(listKey, 0, -1).toArray(new String[0]);
            Entry[] contacts = new Entry[entryKeys.length];
            //Arrays to maintain sorted ordering since a for:each isn't guaranteed to iterate in order
            for (int i = 0; i < entryKeys.length; i++)
                contacts[i] = getEntry(user, entryKeys[i]);
            return contacts;
        } else {
            Entry[] defaultArray = {new Entry("","","","","",0)};
            return defaultArray;
        }
    }

    //Adds a contact to a users contactList

    public static boolean addEntry(User user, String firstName, String lastName, String address,
                                   String email, String phone) {

        if(user == null || firstName == null || lastName == null || address == null)
            return false;

        String listKey = user.getUsername() + ":contactList";
        String entryKey = firstName.toLowerCase().replaceAll(" ", "")
                + lastName.toLowerCase().replaceAll(" ", "");

        boolean isNewKey = (jedis.zadd(listKey, 0, entryKey, ZAddParams.zAddParams().nx()) == 1);

        int duplicateIndex = 0;
        while (!isNewKey) {
            duplicateIndex++;
            isNewKey = (jedis.zadd(listKey, 0, entryKey + duplicateIndex,
                    ZAddParams.zAddParams().nx()) == 1);
        }
        //If it was not a new key, append an index to the stored key
        if(duplicateIndex != 0)
            entryKey = entryKey + duplicateIndex;

        String hKey = user.getUsername() + ":" + entryKey;
        jedis.hset(hKey, "firstName", firstName);
        jedis.hset(hKey, "lastName", lastName);
        jedis.hset(hKey, "address", address);
        jedis.hset(hKey, "index", Integer.toString(duplicateIndex));
        if (email != null)
            jedis.hset(hKey, "email", email);
        if (phone != null)
            jedis.hset(hKey, "phone", phone);

        return true;
    }

    //Gets all info pertaining to an entry

    public static Entry getEntry(User user, String entryKey) {

        String hKey = user.getUsername() + ":" + entryKey;
        Map<String, String> value = jedis.hgetAll(hKey);
        String firstName = value.get("firstName");
        String lastName = value.get("lastName");
        String address = value.get("address");
        String email = value.get("email");
        String phone = value.get("phone");
        int index = Integer.parseInt(value.get("index"));
        return (new Entry(firstName, lastName, email, address, phone, index));
    }

    //Searches entries by first name last name case and space insensitive

    public static Entry[] searchEntries(User user, String search) {

        if(search.contains(" "))
            search = search.replaceAll(" ", "");

        String listKey = user.getUsername() + ":contactList";
        String min = "[" + search.toLowerCase();

        // starting with the search term
        String max = "[" + search.toLowerCase() + "~";
        String[] resultKeys = jedis.zrangeByLex(listKey, min, max).toArray(new String[0]);
        Entry[] result = new Entry[resultKeys.length];
        for(int i = 0; i < resultKeys.length; i++)
            result[i] = getEntry(user, resultKeys[i]);
        return result;
    }

    // Updates an entry in the database
    public static void updateEntry(User user, Entry entry, String newFirstName, String newLastName, String address, String email, String phone) {
        //If the first and last name didn't change, we don't need to change out keys
        if(entry.getFirstName().equalsIgnoreCase(newFirstName) && entry.getLastName().equalsIgnoreCase(newLastName)) {
            String hkey = user.getUsername() + ":" + entry.getKeyString();
            jedis.hset(hkey, "firstName", newFirstName);
            jedis.hset(hkey, "lastName", newLastName);
            jedis.hset(hkey, "address", address);
            if(email != null)
                jedis.hset(hkey, "email", email);
            if(phone != null)
                jedis.hset(hkey, "phone", phone);
        } else {
            //Keys need to be changed to maintain sort ordering
            delete(user, entry);
            addEntry(user, newFirstName, newLastName, address, email, phone);
        }

    }

    //Deletes an entry from the database

    public static void delete(User user, Entry entry) {
        String keyName = entry.getKeyString();
        jedis.zrem(user.getUsername() + ":contactList", keyName);
        jedis.del(user.getUsername() + ":" + keyName);
    }
}
