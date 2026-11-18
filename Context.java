/** 
 * @class: Context
 * This class constructs Context object that has attributes : 
 * 1. lexicalLevel    : current lexical level
 * 2. orderNumber     : current order number
 * 3. symbolHash      : hash table of symbols
 * 4. symbolStack     : stack to keep symbol's name
 * 4. typeStack       : stack to keep symbol's type
 * 4. printSymbols    : choice of printing symbols
 * 5. errorCount      : error counter of context checking
 * 6. routineBuckets  : stack of function/procedure buckets
 * 7. argCountStack   : stack of argument/parameter counters
 *
 * @author: DAJI Group (Dalton E. Pelawi & Jimmy)
 */

import java.util.Stack;

class Context
{
    public Context()
    {
        lexicalLevel = -1;
        orderNumber = 0;
        symbolHash = new Hash(HASH_SIZE);
        symbolStack = new Stack();
        typeStack = new Stack();
        routineBuckets = new Stack();
        argCountStack = new Stack();
        printSymbols = false;
        errorCount = 0;
        pendingRoutine = false;
        parameterMode = false;
        routineName = null;
    }

    /**
     * This method chooses which action to be taken
     * @input : ruleNo(type:int)
     * @output: -(type:void)
     */
    public void C(int ruleNo)
    {
        boolean error = false;

        //System.out.println("C" + ruleNo);
        switch(ruleNo)
        {
            case 0:
                lexicalLevel++;
                orderNumber = 0;

                // C22 sudah menandai bahwa scope berikutnya adalah badan
                // sebuah fungsi/prosedur. Simpan bucket rutin agar R42/R43
                // dapat membaca jumlah parameter saat membangkitkan return.
                if (pendingRoutine)
                {
                    Bucket routine = symbolHash.find(routineName);

                    routineBuckets.push(routine);
                    pendingRoutine = false;
                }
                break;
            case 1:
                if (printSymbols)
                    symbolHash.print(lexicalLevel);
                break;
            case 2:
                symbolHash.delete(lexicalLevel);
                lexicalLevel--;
                break;
            case 3:
                if (symbolHash.isExist(currentStr, lexicalLevel))
                {
                    System.out.println("Variable declared at line " + currentLine + ": " + currentStr);
                    errorCount++;
                    System.err.println("\nProcess terminated.\nAt least " + (errorCount + parser.yylex.num_error)
                                       + " error(s) detected.");
                    System.exit(1);
                }
                else
                {
                    symbolHash.insert(new Bucket(currentStr));
                }
                symbolStack.push(currentStr);
                break;
            case 4:
                symbolHash.find(currentStr).setLLON(lexicalLevel, orderNumber);
                break;
            case 5:
                int idType = ((Integer)typeStack.peek()).intValue();
                Bucket symbol = symbolHash.find(currentStr);

                symbol.setIdType(idType);

                // C25 menandai bahwa simbol yang baru dideklarasikan
                // adalah parameter. C5 lalu mencatat tipenya pada rutin.
                if (parameterMode)
                {
                    Bucket routine = (Bucket)routineBuckets.peek();
                    routine.addParamType(idType);
                }
                break;
            case 6:
                if (!symbolHash.isExist(currentStr))
                {
                    System.out.println("Variable undeclared at line " + currentLine + ": " + currentStr);
                    errorCount++;
                    System.err.println("\nProcess terminated.\nAt least " + (errorCount + parser.yylex.num_error)
                                       + " error(s) detected.");
                    System.exit(1);
                }
                else
                {
                    symbolStack.push(currentStr);
                }
                break;
            case 7:
                symbolStack.pop();
                parameterMode = false;
                break;
            case 8:
                // Gunakan nama di symbolStack, bukan currentStr, karena C8
                // pada pemanggilan fungsi dieksekusi setelah argumen-argumen
                // (yang bisa saja mengubah currentStr).
                symbol = symbolHash.find((String)symbolStack.peek());
                typeStack.push(new Integer(symbol.getIdType()));
                break;
            case 9:
                typeStack.push(new Integer(Bucket.INTEGER));
                break;
            case 10:
                typeStack.push(new Integer(Bucket.BOOLEAN));
                break;
            case 11:
                typeStack.pop();
                break;
            case 12:
                switch (((Integer)typeStack.peek()).intValue())
                {
                    case Bucket.BOOLEAN:
                        System.out.println("Type of integer expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.UNDEFINED:
                        System.out.println("Undefined type at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                }
                break;
            case 13:
                switch (((Integer)typeStack.peek()).intValue())
                {
                    case Bucket.INTEGER:
                        System.out.println("Type of boolean expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.UNDEFINED:
                        System.out.println("Undefined type at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                }
                break;
            case 14:
                int temp = ((Integer)typeStack.pop()).intValue();
                if (temp != ((Integer)typeStack.peek()).intValue())
                {
                    System.out.println("Unmatched type at line " + currentLine + ": " + currentStr);
                    errorCount++;
                }
                typeStack.push(new Integer(temp));
                break;
            case 15:
                temp = ((Integer)typeStack.pop()).intValue();
                if ((temp != Bucket.INTEGER) && ((Integer)typeStack.peek()).intValue() != Bucket.INTEGER)
                {
                    System.out.println("Unmatched type at line " + currentLine + ": " + currentStr);
                    errorCount++;
                }
                typeStack.push(new Integer(temp));
                break;
            case 16:
                temp = symbolHash.find((String)symbolStack.peek()).getIdType();
                if (temp != ((Integer)typeStack.peek()).intValue())
                {
                    System.out.println("Unmatched type at line " + currentLine + ": " + currentStr);
                    errorCount++;
                }
                break;
            case 17:
                symbol = symbolHash.find((String)symbolStack.peek());

                if (symbol.getIdKind() == Bucket.FUNCTION ||
                    symbol.getIdKind() == Bucket.PROCEDURE)
                {
                    System.out.println("Variable expected at line " + currentLine + ": " + symbol.getIdName());
                    errorCount++;
                }
                else if (symbol.getIdType() != Bucket.INTEGER)
                {
                    System.out.println("Type of integer expected at line " + currentLine + ": " + currentStr);
                    errorCount++;
                }
                break;
            case 18:
                symbolHash.find(currentStr).setIdKind(Bucket.SCALAR);
                orderNumber++;
                break;
            case 19:
                symbolHash.find(currentStr).setIdKind(Bucket.ARRAY);
                orderNumber += 3;
                break;
            case 20:
                switch (symbolHash.find((String)symbolStack.peek()).getIdKind())
                {
                    case Bucket.UNDEFINED:
                        System.out.println("Variable not fully defined at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.ARRAY:
                        System.out.println("Scalar variable expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.FUNCTION:
                    case Bucket.PROCEDURE:
                        System.out.println("Variable expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                }
                break;
            case 21:
                switch (symbolHash.find((String)symbolStack.peek()).getIdKind())
                {
                    case Bucket.UNDEFINED:
                        System.out.println("Variable not fully defined at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.SCALAR:
                        System.out.println("Array variable expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                    case Bucket.FUNCTION:
                    case Bucket.PROCEDURE:
                        System.out.println("Variable expected at line " + currentLine + ": " + currentStr);
                        errorCount++;
                        break;
                }
                break;
            case 22:
                // Masukkan lexical level dan entry point rutin ke tabel
                // simbol. C22 dieksekusi setelah R7, sehingga cell saat ini
                // adalah awal badan fungsi/prosedur.
                Bucket routine = symbolHash.find(currentStr);

                routine.setLexicLev(lexicalLevel);
                routine.setOrderNum(parser.generate.getCell());
                routine.resetParameters();

                routineName = currentStr;
                pendingRoutine = true;
                break;
            case 23:
                // Masukkan type int/bool milik fungsi.
                symbolHash.find(currentStr).setIdType(
                    ((Integer)typeStack.peek()).intValue());
                break;
            case 24:
                // Masukkan prosedur ke tabel simbol.
                symbolHash.find(currentStr).setIdKind(Bucket.PROCEDURE);
                break;
            case 25:
                // Masukkan parameter ke tabel simbol.
                // Parameter berada di bawah frame base (dipush pemanggil),
                // dengan urutan argumen dibalik saat pemanggilan.
                Bucket routineOwner = (Bucket)routineBuckets.peek();
                Bucket parameter = symbolHash.find(currentStr);
                int paramIndex = getArgumentCount();
                int offset;

                if (routineOwner.getIdKind() == Bucket.FUNCTION)
                    offset = -(paramIndex + 3);
                else
                    offset = -(paramIndex + 2);

                parameter.setLLON(lexicalLevel, offset);
                parameter.setIdKind(Bucket.SCALAR);
                parameterMode = true;
                break;
            case 26:
                // Masukkan fungsi ke tabel simbol.
                symbolHash.find(currentStr).setIdKind(Bucket.FUNCTION);
                break;
            case 27:
                // Keluar dari scope yang mengandung parameter.
                symbolHash.delete(lexicalLevel);
                lexicalLevel--;
                parameterMode = false;
                break;
            case 28:
                // Periksa bahwa identifier adalah nama prosedur.
                symbol = symbolHash.find((String)symbolStack.peek());
                if (symbol.getIdKind() != Bucket.PROCEDURE)
                {
                    System.out.println("Procedure expected at line " + currentLine +
                                       ": " + symbol.getIdName());
                    errorCount++;
                }
                break;
            case 29:
                // Periksa bahwa fungsi/prosedur tidak punya parameter
                // (untuk pemanggilan tanpa tanda kurung).
                symbol = symbolHash.find((String)symbolStack.peek());
                if ((symbol.getIdKind() == Bucket.FUNCTION ||
                     symbol.getIdKind() == Bucket.PROCEDURE) &&
                    symbol.getParamCount() != 0)
                {
                    System.out.println("No-argument routine expected at line " + currentLine +
                                       ": " + symbol.getIdName());
                    errorCount++;
                }
                break;
            case 30:
                // Push jumlah argumen = 0.
                argCountStack.push(new Integer(0));
                break;
            case 31:
                // Periksa argumen terhadap parameter.
                symbol = symbolHash.find((String)symbolStack.peek());
                if (symbol.getIdKind() == Bucket.FUNCTION ||
                    symbol.getIdKind() == Bucket.PROCEDURE)
                {
                    int index = getArgumentCount() - 1;

                    if (index >= 0 && index < symbol.getParamCount())
                    {
                        int actualType = ((Integer)typeStack.peek()).intValue();
                        int formalType = symbol.getParamType(index);

                        if (actualType != Bucket.UNDEFINED &&
                            formalType != Bucket.UNDEFINED &&
                            actualType != formalType)
                        {
                            System.out.println("Argument type mismatch at line " + currentLine +
                                               ": " + symbol.getIdName() +
                                               " parameter " + (index + 1));
                            errorCount++;
                        }
                    }
                }
                break;
            case 32:
                // Periksa bahwa semua argumen sudah dilihat.
                // Pop jumlah argumen.
                int actualCount = popArgumentCount();
                symbol = symbolHash.find((String)symbolStack.peek());

                if (actualCount != symbol.getParamCount())
                {
                    System.out.println("Argument count mismatch at line " + currentLine +
                                       ": " + symbol.getIdName() + " (expected " +
                                       symbol.getParamCount() + ", found " +
                                       actualCount + ")");
                    errorCount++;
                }
                break;
            case 33:
                // Periksa bahwa identifier adalah nama fungsi.
                symbol = symbolHash.find((String)symbolStack.peek());
                if (symbol.getIdKind() != Bucket.FUNCTION)
                {
                    System.out.println("Function expected at line " + currentLine +
                                       ": " + symbol.getIdName());
                    errorCount++;
                }
                break;
            case 34:
                // Tambah nilai jumlah argumen.
                incrementArgumentCount();
                break;
            case 35:
                // Masukkan jumlah parameter ke tabel simbol.
                // Pop jumlah argumen.
                int parameterCount = popArgumentCount();
                symbol = symbolHash.find((String)symbolStack.peek());
                symbol.setParamCount(parameterCount);
                break;
            case 36:
                // Periksa bahwa type ekspresi sama dengan type fungsi.
                symbol = symbolHash.find((String)symbolStack.peek());
                if (symbol.getIdType() != ((Integer)typeStack.peek()).intValue())
                {
                    System.out.println("Unmatched type at line " + currentLine +
                                       ": " + symbol.getIdName());
                    errorCount++;
                }
                break;
            case 37:
                // Jika identifier suatu fungsi maka C33, else C20.
                symbol = symbolHash.find((String)symbolStack.peek());
                if (symbol.getIdKind() == Bucket.FUNCTION)
                    C(33);
                else
                    C(20);
                break;
        }
    }

    /**
     * Jumlah argumen/parameter pada konteks teratas.
     */
    private int getArgumentCount()
    {
        if (argCountStack.isEmpty())
            return 0;

        return ((Integer)argCountStack.peek()).intValue();
    }

    private void incrementArgumentCount()
    {
        if (!argCountStack.isEmpty())
        {
            int count = ((Integer)argCountStack.pop()).intValue();
            argCountStack.push(new Integer(count + 1));
        }
    }

    private int popArgumentCount()
    {
        if (argCountStack.isEmpty())
            return 0;

        return ((Integer)argCountStack.pop()).intValue();
    }

    /**
     * Bucket rutin yang sedang dibangkitkan kodenya.
     */
    public Bucket currentRoutine()
    {
        return (Bucket)routineBuckets.peek();
    }

    /**
     * Selesai membangkitkan kode badan rutin (dipanggil R42/R43).
     */
    public void endRoutine()
    {
        if (!routineBuckets.isEmpty())
            routineBuckets.pop();
    }

    /**
     * This method sets the current token and line
     * @input : str(type:int), line(type:int)
     * @output: -(type:void)
     */
    public void setCurrent(String str, int line)
    {
        currentStr = str;
        currentLine = line;
    }

    /**
     * This method sets symbol printing option
     * @input : bool(type:boolean)
     * @output: -(type:void)
     */
    public void setPrint(boolean bool)
    {
        printSymbols = bool;
    }

    private final int HASH_SIZE = 211;

    public static int lexicalLevel;
    public static int orderNumber;
    public static Hash symbolHash;
    private Stack symbolStack;
    private Stack typeStack;
    private Stack routineBuckets;
    private Stack argCountStack;
    public static String currentStr;
    public static int currentLine;
    private boolean printSymbols;
    public int errorCount;
    private boolean pendingRoutine;
    private boolean parameterMode;
    private String routineName;
}