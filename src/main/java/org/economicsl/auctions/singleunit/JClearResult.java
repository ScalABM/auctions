/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.Tradable;
import scala.Option;

import java.util.stream.Stream;


/**
 *
 * @param <T>
 * @param <A>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JClearResult<T extends Tradable, A> {

    final private Option<Stream<Fill<T>>> fills;
    final private A residual;

    public JClearResult(Option<Stream<Fill<T>>> fills, A residual) {
        this.fills = fills;
        this.residual = residual;
    }

    public Option<Stream<Fill<T>>> getFills() {
        return this.fills;
    }

    public A getResidual() {
        return this.residual;
    }

}