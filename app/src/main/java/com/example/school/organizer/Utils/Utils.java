package com.example.school.organizer.Utils;

import android.location.Address;

import com.example.school.organizer.AddressRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Iliyan Petrov on 8/28/2016.
 * Helper class when working with custom ListView and custom Adapter.
 *
 * @see com.example.school.organizer.MapsActivity.SelectArralAdapter
 * @see com.example.school.organizer.SelectViewHolder
 */
public class Utils {

    /**
     * Will remove not checked elements from addressFromName according to addresses.
     * @param addressesFromName ArrayList with Address that are going to be reduced on @addresses base
     * @param addresses Checked items from the UI ListView
     * @see Address
     */
    public static void leaveOnlyCheckedAddresses(ArrayList<Address> addressesFromName,
                                           ArrayList<AddressRow> addresses){
        if ( addresses.size() > 0 ) {
            Iterator<AddressRow> iterator = addresses.iterator();
            Iterator<Address> itrAddressesFromName = addressesFromName.iterator();

            while(iterator.hasNext() && itrAddressesFromName.hasNext()) {
                itrAddressesFromName.next();
                if (!iterator.next().isChecked()) {
                    iterator.remove();
                    itrAddressesFromName.remove();
                }
            }
        }
    }

    /**
     * Will convert List with Address to ArrayList with AddressRow
     * @param source source List
     * @param target target ArrayList
     */
    public static synchronized void convertArrayAddressToArrayString(List<Address> source, ArrayList<AddressRow> target) {
        StringBuilder addressLine = new StringBuilder();

        if (source != null && target != null) {
            int counter = 1;

            for (int i = target.size(); i < source.size(); i++) {
                Address object = source.get(i);

                for (int j = 0; j < object.getMaxAddressLineIndex(); j++) {
                    addressLine.append(object.getAddressLine(j) + " ");
                }
                target.add(new AddressRow(counter + ": Country: " + object.getCountryName() +
                        "\nAddress: " + addressLine));
                addressLine.delete( 0, addressLine.length() );

                counter++;
            }
        }
    }

}
