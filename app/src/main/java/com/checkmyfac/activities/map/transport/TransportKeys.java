package com.checkmyfac.activities.map.transport;

import android.content.Context;

import com.checkmyfac.R;

/**
 * TransportKeys: Enum des mots clés de l'api des horaires
 */
public enum TransportKeys {

    // https://github.com/pgrimaud/horaires-ratp-api#lignes
    rers(R.string.RER),
    metros(R.string.Metro),
    tramways(R.string.Tramway),
    bus(R.string.Bus),
    noctiliens(R.string.Noctilien);

    private int resId;

    TransportKeys(int id) {
        resId = id;
    }

    public String display(Context ctx) {
        return ctx.getString(resId);
    }
}
