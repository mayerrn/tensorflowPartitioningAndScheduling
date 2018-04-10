# https://www.tensorflow.org/versions/r0.11/tutorials/mnist/beginners/index.html

import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data


costmodelFile = open('costmodel_simple_mnist_example', 'w')
graphFile = open('graph_simple_mnist_example', 'w')

mnist = input_data.read_data_sets("MNIST_data/", one_hot=True)
x = tf.placeholder(tf.float32, [None, 784])
W = tf.Variable(tf.zeros([784, 10]))
b = tf.Variable(tf.zeros([10]))
y = tf.nn.softmax(tf.matmul(x, W) + b)
y_ = tf.placeholder(tf.float32, [None, 10])
cross_entropy = tf.reduce_mean(-tf.reduce_sum(y_ * tf.log(y), reduction_indices=[1]))
train_step = tf.train.GradientDescentOptimizer(0.5).minimize(cross_entropy)
init = tf.initialize_all_variables()
options = tf.GraphOptions(build_cost_model=50)
cfg = tf.ConfigProto(graph_options=options)
sess = tf.Session(config=cfg)
metadata = tf.RunMetadata()
# This is optional, but will generally give you more accurate statistics,
run_options = tf.RunOptions(trace_level=tf.RunOptions.FULL_TRACE)
sess.run(init)
for i in range(1000):
	batch_xs, batch_ys = mnist.train.next_batch(100)
	sess.run(train_step, feed_dict={x: batch_xs, y_: batch_ys},options=run_options, run_metadata=metadata)
	if len(metadata.cost_graph.node) > 0:
		costmodelFile.write(str(metadata.cost_graph))
correct_prediction = tf.equal(tf.argmax(y,1), tf.argmax(y_,1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
graphFile.write(str(tf.get_default_graph().as_graph_def()))
print(sess.run(accuracy, feed_dict={x: mnist.test.images, y_: mnist.test.labels}))
