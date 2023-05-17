package apotee.sky.poc1.commands;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import apotee.sky.poc1.ApiReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//import apotee.sky.poc1.Manifest;

public class CommandGetContacts {
    public static int MINIMUM_APP_VERSION = 100;
    public static String cmd = "cmd_get_contacts";
    public static String[] permissions = {Manifest.permission.READ_CONTACTS};

    public static void onReceive(final ApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, out -> {
            JSONObject res = getAllContacts(context.getContentResolver());
            out.print(res.toString(1));
        });
    }

    // https://www.dev2qa.com/how-to-get-contact-list-in-android-programmatically/
    public static JSONObject getAllContacts(ContentResolver cr) {
        JSONArray res_val = new JSONArray();
        JSONObject res = new JSONObject();


        List<Integer> rawContactsIdList = getRawContactsIdList(cr);
        int contactListSize = rawContactsIdList.size();

        try {
            res.put("contacts_size", contactListSize);

            if (contactListSize > 50) {
                contactListSize = 50;
            }

            for (int i = 0; i < contactListSize; i++) {
                JSONObject c = new JSONObject();
                // Get the raw contact id.
                Integer rawContactId = rawContactsIdList.get(i);


                // Data content uri (access data table. )
                Uri dataContentUri = ContactsContract.Data.CONTENT_URI;

                // Build query columns name array.
                List<String> queryColumnList = new ArrayList<>();

                // ContactsContract.Data.CONTACT_ID = "contact_id";
                queryColumnList.add(ContactsContract.Data.CONTACT_ID);

                /*

                    protected interface ContactOptionsColumns {
        String CUSTOM_RINGTONE = "custom_ringtone";
        String LAST_TIME_CONTACTED = "last_time_contacted";
        String PINNED = "pinned";
        String SEND_TO_VOICEMAIL = "send_to_voicemail";
        String STARRED = "starred";
        String TIMES_CONTACTED = "times_contacted";
    }

    /data/user/0/com.android.providers.contacts/databases/contacts2.db

    custom_ringtone TEXT,
    send_to_voicemail INTEGER NOT NULL DEFAULT 0,
    x_times_contacted INTEGER NOT NULL DEFAULT 0,
    x_last_time_contacted INTEGER,
    times_contacted INTEGER NOT NULL DEFAULT 0,
    last_time_contacted INTEGER,
    starred INTEGER NOT NULL DEFAULT 0,
    pinned INTEGER NOT NULL DEFAULT 0,
    has_phone_number INTEGER NOT NULL DEFAULT 0,
    lookup TEXT,
    status_update_id INTEGER REFERENCES data(_id),
    contact_last_updated_timestamp INTEGER)

   */

                queryColumnList.add(ContactsContract.Data.CUSTOM_RINGTONE);
                queryColumnList.add(ContactsContract.Data.LAST_TIME_CONTACTED);
                queryColumnList.add(ContactsContract.Data.PINNED);
                queryColumnList.add(ContactsContract.Data.SEND_TO_VOICEMAIL);
                queryColumnList.add(ContactsContract.Data.STARRED);
                queryColumnList.add(ContactsContract.Data.TIMES_CONTACTED);
                queryColumnList.add(ContactsContract.Data.CONTACT_PRESENCE);
                queryColumnList.add(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP);


                // ContactsContract.Data.MIMETYPE = "mimetype";
                queryColumnList.add(ContactsContract.Data.MIMETYPE);


                //cursor.getInt(cursor.getInt(ContactsContract.PinnedPositions.));

                queryColumnList.add(ContactsContract.Data.DATA1);
                queryColumnList.add(ContactsContract.Data.DATA2);
                queryColumnList.add(ContactsContract.Data.DATA3);
                queryColumnList.add(ContactsContract.Data.DATA4);
                queryColumnList.add(ContactsContract.Data.DATA5);
                queryColumnList.add(ContactsContract.Data.DATA6);
                queryColumnList.add(ContactsContract.Data.DATA7);
                queryColumnList.add(ContactsContract.Data.DATA8);
                queryColumnList.add(ContactsContract.Data.DATA9);
                queryColumnList.add(ContactsContract.Data.DATA10);
                queryColumnList.add(ContactsContract.Data.DATA11);
                queryColumnList.add(ContactsContract.Data.DATA12);
                queryColumnList.add(ContactsContract.Data.DATA13);
                queryColumnList.add(ContactsContract.Data.DATA14);
                queryColumnList.add(ContactsContract.Data.DATA15);

                // Translate column name list to array.
                String[] queryColumnArr = queryColumnList.toArray(new String[0]);

                // Build query condition string. Query rows by contact id.

                // Query data table and return related contact data.
                String whereClauseBuf = ContactsContract.Data.RAW_CONTACT_ID +
                        "=" +
                        rawContactId;
                Cursor cursor = cr.query(dataContentUri, queryColumnArr, whereClauseBuf, null, null);

                if (cursor != null && cursor.getCount() > 0) {
                    //StringBuffer lineBuf = new StringBuffer();
                    cursor.moveToFirst();


                    c.put("raw_contact_id", rawContactId);

                    c.put("carrier_presence", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_PRESENCE)));
                    c.put("contacted_last_update_timestamp", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_LAST_UPDATED_TIMESTAMP)));
                    c.put("last_time_contacted", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.LAST_TIME_CONTACTED)));
                    c.put("starred", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.STARRED)));


                    long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    c.put("contact_id", contactId);
                    do {
                        String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

                        List<String> dataValueList = getColumnValueByMimetype(cursor, mimeType);
                        int dataValueListSize = dataValueList.size();
                        for (int j = 0; j < dataValueListSize; j++) {
                            c.put(mimeType + "_" + j, dataValueList.get(j));
                        }

                    } while (cursor.moveToNext());

                }

                res_val.put(c);

            }

            res.put("contacts", res_val);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    // Return all raw_contacts _id in a list.
    public static List<Integer> getRawContactsIdList(ContentResolver contentResolver) {
        List<Integer> ret = new ArrayList<>();

        // Row contacts content uri( access raw_contacts table. ).
        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI;
        // Return _id column in contacts raw_contacts table.
        String[] queryColumnArr = {ContactsContract.RawContacts._ID};
        // Query raw_contacts table and return raw_contacts table _id.
        Cursor cursor = contentResolver.query(rawContactUri, queryColumnArr, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            do {
                int idColumnIndex = cursor.getColumnIndex(ContactsContract.RawContacts._ID);
                int rawContactsId = cursor.getInt(idColumnIndex);
                ret.add(rawContactsId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return ret;
    }

    /*
     *  Return data column value by mimetype column value.
     *  Because for each mimetype there has not only one related value,
     *  such as Organization.CONTENT_ITEM_TYPE need return company, department, title, job description etc.
     *  So the return is a list string, each string for one column value.
     * */
    public static List<String> getColumnValueByMimetype(Cursor cursor, String mimeType) {
        List<String> ret = new ArrayList<>();

        switch (mimeType) {
            // Get email data.
            case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                // Email.ADDRESS == data1
                String emailAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                // Email.TYPE == data2
                int emailType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                ret.add("email_type:" + emailType);
                ret.add("Email Address : " + emailAddress);
                ret.add("Email Int Type : " + emailType);
                break;

            // Get im data.
            case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE:
                // Im.PROTOCOL == data5
                String imProtocol = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
                // Im.DATA == data1
                String imId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));

                ret.add("IM Protocol : " + imProtocol);
                ret.add("IM ID : " + imId);
                break;

            // Get nickname
            case ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
                // Nickname.NAME == data1
                String nickName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                ret.add("Nick name : " + nickName);
                break;

            // Get organization data.
            case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
                // Organization.COMPANY == data1
                String company = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                // Organization.DEPARTMENT == data5
                String department = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
                // Organization.TITLE == data4
                String title = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                // Organization.JOB_DESCRIPTION == data6
                String jobDescription = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION));
                // Organization.OFFICE_LOCATION == data9
                String officeLocation = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION));

                ret.add("Company : " + company);
                ret.add("department : " + department);
                ret.add("Title : " + title);
                ret.add("Job Description : " + jobDescription);
                ret.add("Office Location : " + officeLocation);
                break;

            // Get phone number.
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                // Phone.NUMBER == data1
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                // Phone.TYPE == data2
                int phoneTypeStr = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                ret.add("phone_type:" + phoneTypeStr);
                ret.add("Phone Number : " + phoneNumber);
                break;

            // Get sip address.
            case ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE:
                // SipAddress.SIP_ADDRESS == data1
                String address = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS));
                // SipAddress.TYPE == data2
                int addressTypeInt = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE));
                ret.add("address_type:" + addressTypeInt);
                ret.add("Address : " + address);
                ret.add("Address Type Integer : " + addressTypeInt);
                break;

            // Get display name.
            case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                // StructuredName.DISPLAY_NAME == data1
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                // StructuredName.GIVEN_NAME == data2
                String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                // StructuredName.FAMILY_NAME == data3
                String familyName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));

                ret.add("Display Name : " + displayName);
                ret.add("Given Name : " + givenName);
                ret.add("Family Name : " + familyName);
                break;

            // Get postal address.
            case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
                // StructuredPostal.COUNTRY == data10
                String country = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                // StructuredPostal.CITY == data7
                String city = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                // StructuredPostal.REGION == data8
                String region = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                // StructuredPostal.STREET == data4
                String street = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                // StructuredPostal.POSTCODE == data9
                String postcode = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                // StructuredPostal.TYPE == data2
                int postType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

                ret.add("post_type: " + postType);
                ret.add("Country : " + country);
                ret.add("City : " + city);
                ret.add("Region : " + region);
                ret.add("Street : " + street);
                ret.add("Postcode : " + postcode);
                ret.add("Post Type Integer : " + postType);
                break;

            // Get identity.
            case ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE:
                // Identity.IDENTITY == data1
                String identity = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.IDENTITY));
                // Identity.NAMESPACE == data2
                String namespace = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Identity.NAMESPACE));

                ret.add("Identity : " + identity);
                ret.add("Identity Namespace : " + namespace);
                break;

            // Get photo.
            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
                // Photo.PHOTO == data15
                String photo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
                // Photo.PHOTO_FILE_ID == data14
                String photoFileId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));

                ret.add("Photo : " + photo);
                ret.add("Photo File Id: " + photoFileId);
                break;

            // Get group membership.
            case ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE:
                // GroupMembership.GROUP_ROW_ID == data1
                int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
                ret.add("Group ID : " + groupId);
                break;

            // Get website.
            case ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE:
                // Website.URL == data1
                String websiteUrl = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                // Website.TYPE == data2
                int websiteTypeInt = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
                ret.add("website_type:" + websiteTypeInt);
                ret.add("Website Url : " + websiteUrl);
                ret.add("Website Type Integer : " + websiteTypeInt);
                break;

            // Get note.
            case ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE:
                // Note.NOTE == data1
                String note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                ret.add("Note : " + note);
                break;

            default:
                ret.add("unknown");
                break;

        }

        return ret;
    }

    public static String getEmailTypeString(int dataType) {
        String ret = "";

        if (ContactsContract.CommonDataKinds.Email.TYPE_HOME == dataType) {
            ret = "Home";
        } else if (ContactsContract.CommonDataKinds.Email.TYPE_WORK == dataType) {
            ret = "Work";
        }
        return ret;
    }


}
