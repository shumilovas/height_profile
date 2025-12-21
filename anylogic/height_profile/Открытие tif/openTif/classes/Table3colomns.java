/**
 * Table3colomns
 */	
public class Table3colomns implements Serializable {

	public int x = 1;

	public int y = 1;

	public int h = 1;

    /**
     * Конструктор по умолчанию
     */
    public Table3colomns() {
    }

    /**
     * Конструктор, инициализирующий поля
     */
    public Table3colomns(int x, int y, int h) {
		this.x = x;
		this.y = y;
		this.h = h;
    }

	@Override
	public String toString() {
		return  
			"x = " + x +" " +
			"y = " + y +" " +
			"h = " + h +" ";
	}

	/**
	 * Это число используется при сохранении состояния модели<br>
	 * Его рекомендуется изменить в случае изменения класса
	 */ 
	private static final long serialVersionUID = 1L;

}