/*
 * Created on Dec 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.lnicholls.galleon.media;

import java.util.*;

import org.lnicholls.galleon.media.*;

/**
 * @author Owner
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Player {

    public void init(List list);
    
    public Media getNext();
    
    public Media getPrevious();
    
    public Media getFirst();
    
    public Media getLast();    
    
    public void forward(int speed);
    
    public void backward(int speed);
    
    public void play();
    
    public void pause();
    
    public void stop();
    
    public void first();
    
    public void last();
    
}
