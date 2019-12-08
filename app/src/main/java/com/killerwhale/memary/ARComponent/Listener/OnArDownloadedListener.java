package com.killerwhale.memary.ARComponent.Listener;

import com.killerwhale.memary.ARComponent.Model.Stroke;

import java.util.List;

/**
 * Interface
 * Called when all download task is finished
 * @author Zeyu Fu
 */
public interface OnArDownloadedListener {
    void setStrokeList(List<List<Stroke>> ar);
}
