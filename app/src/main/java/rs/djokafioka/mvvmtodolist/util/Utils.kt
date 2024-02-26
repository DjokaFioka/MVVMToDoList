package rs.djokafioka.mvvmtodolist.util

/**
 * Created by Djordje on 12.8.2022..
 */
//Napravili da bismo od statement-a napravili expression da kada koristimo when nece da kompajlira kod ako nismo pokrili sve slucajeve
//Ovo je zapravo Extension function koja moze da se napravi na bilo kojoj klasi, samo je ovde upotrebljen generik
val <T> T.exhaustive: T
    get() = this