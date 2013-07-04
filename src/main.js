goog.provide('jsa');
goog.require('cljs.core');
jsa.max_volume = 0.4;
jsa.let$.call(null,cljs.core.PersistentVector.fromArray([jsa.buf,jsa.array.call(null)], true),cljs.core._STAR_print_fn_STAR_ = jsa.fn.call(null,cljs.core.PersistentVector.fromArray([jsa.s], true),jsa.if_let.call(null,cljs.core.PersistentVector.fromArray([cljs.core.PersistentVector.fromArray([jsa._,jsa.pre,jsa.post], true),jsa.re_find.call(null,/^(.*)\n([^\n]*)/m,jsa.s)], true),(function (){jsa.buf.push(jsa.pre);
console.log(jsa.buf.join(""));
return jsa.buf.splice(0,jsa.buf.length,jsa.post);
})(),jsa.buf.push(jsa.s))));
jsa.defn.call(null,jsa.setup_context,"Set up an audio context needed for playing sounds.",cljs.core.PersistentVector.EMPTY,jsa.when_let.call(null,cljs.core.PersistentVector.fromArray([jsa.ctor,jsa.or.call(null,window.AudioContext,window.webkitAudioContext)], true),jsa.let$.call(null,cljs.core.PersistentVector.fromArray([jsa.audio_context,(new jsa.ctor()),jsa.compressor,jsa.audio_context.createDynamicsCompressor()], true),jsa.compressor.connect(jsa.audio_context.destination),cljs.core.PersistentArrayMap.fromArray(["\uFDD0:audio-context",jsa.audio_context,"\uFDD0:output",jsa.compressor], true))));
