1.保持最新的数据在log/Jemson_etl文件夹中，文件为post.log
2.一直保持读log/nginx.log文件，读完了，关闭输入输出流，再重新读log/nginx.log
3.每一个小时滚动一下post.log，修改其为post.log_14-00-00并把它移进2019-06-06文件夹,重新创建一个post.log