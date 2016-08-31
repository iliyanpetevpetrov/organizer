package com.example.school.organizer.Utils;

import android.location.Address;

import com.example.school.organizer.AddressRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jeff's world on 8/28/2016.
 */
public class Utils {

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

    public static synchronized void convertArrayAddressToArrayString(List<Address> source, ArrayList<AddressRow> target) {
        StringBuilder addressLine = new StringBuilder();

        if (source != null && target != null) {
            int counter = 1;

//            for(Address object: source) {
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
