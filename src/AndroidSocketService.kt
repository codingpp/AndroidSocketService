import java.io.*
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidSocketService {

    /**
     * 端口号
     */
    val PORT = 8888
    var mClientList = ArrayList<Socket>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AndroidSocketService()
        }

    }

    init {
        try {
            val server = ServerSocket(PORT)
            val mExecutors: ExecutorService = Executors.newCachedThreadPool()
            System.out.println("服务已启动，等待客户端连接...")
            var client: Socket
            //死循环等待客户端连接，连接一个就启动一个线程管理
            while (true) {
                client = server.accept()
                //把客户端放入集合中
                mClientList.add(client)
                //启动线程，守候从客户端发来的消息
                mExecutors.execute(Service(client))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class Service(private var socket: Socket) : Runnable {

        private lateinit var buffer: BufferedReader
        private var message: String = ""

        init {
            try {
                buffer = BufferedReader(InputStreamReader(socket.getInputStream()))
                message = "服务器地址：" + socket.inetAddress
                sendMessage(message)
                message = "当前连接总数：" + mClientList.size
                sendMessage(message)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                while (true) {
                    if (message == "exit") {
                        closeSocket()
                        break
                    } else {
                        message = "" + socket.inetAddress + ":" + message
                        sendMessage(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 关闭客户端
         *
         */
        private fun closeSocket() {
            mClientList.remove(socket)
            buffer.close()
            message = "主机：" + socket.inetAddress + "关闭连接\n目前在线：" + mClientList.size
            socket.close()
            sendMessage(message)
        }

        /**
         * 将接收到的消息转发给每一个客户端
         * @param msg 消息
         */
        private fun sendMessage(msg: String) {
            System.out.println(msg)
            //遍历客户端集合
            mClientList.forEach {
                val out: PrintWriter
                try {
                    //创建输出流对象
                    out = PrintWriter(BufferedWriter(OutputStreamWriter(it.getOutputStream())), true)
                    //转发
                    out.println(msg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }


}