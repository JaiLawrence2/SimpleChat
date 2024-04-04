package edu.jsu.mcis.cs408.simplechat;

import java.beans.PropertyChangeEvent;

public interface AbstractView {

    public abstract void modelPropertyChange(final PropertyChangeEvent evt);

}
