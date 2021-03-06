# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface
  
  class PerferContext
    
    def iterate(name, &block)
      BenchmarkInterface.benchmark name, &block
    end
    
  end
  
end

module Perfer
  
  def self.session(name)
    yield BenchmarkInterface::PerferContext.new
  end
  
end
